/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import com.twofasapp.core.android.ktx.enqueueUniqueIfNotScheduled
import com.twofasapp.core.android.ktx.runSafely
import com.twofasapp.core.common.build.Device
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.core.common.services.CrashlyticsInstance
import com.twofasapp.core.common.time.TimeProvider
import com.twofasapp.data.cloud.domain.CloudResult
import com.twofasapp.data.cloud.domain.VaultMergeResult
import com.twofasapp.data.cloud.domain.VaultSyncRequest
import com.twofasapp.data.cloud.exceptions.CloudError
import com.twofasapp.data.cloud.services.CloudServiceProvider
import com.twofasapp.data.main.BackupRepository
import com.twofasapp.data.main.CloudRepository
import com.twofasapp.data.main.DeletedItemsRepository
import com.twofasapp.data.main.LoginsRepository
import com.twofasapp.data.main.SecurityRepository
import com.twofasapp.data.main.TagsRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.VaultKeysRepository
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.main.domain.CloudMerger
import com.twofasapp.data.main.domain.CloudSyncStatus
import com.twofasapp.data.main.domain.InvalidSchemaVersionException
import com.twofasapp.data.main.domain.VaultBackup
import com.twofasapp.data.purchases.PurchasesRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class CloudSyncWork(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val cloudRepository: CloudRepository by inject()
    private val vaultRepository: VaultsRepository by inject()
    private val vaultKeysRepository: VaultKeysRepository by inject()
    private val backupRepository: BackupRepository by inject()
    private val loginsRepository: LoginsRepository by inject()
    private val tagsRepository: TagsRepository by inject()
    private val deletedItemsRepository: DeletedItemsRepository by inject()
    private val securityRepository: SecurityRepository by inject()
    private val purchasesRepository: PurchasesRepository by inject()
    private val cloudServiceProvider: CloudServiceProvider by inject()
    private val cloudMerger: CloudMerger by inject()
    private val vaultCryptoScope: VaultCryptoScope by inject()
    private val device: Device by inject()
    private val timeProvider: TimeProvider by inject()

    companion object {
        const val ArgForceReplace = "ArgForceReplace"

        fun dispatch(
            context: Context,
            forceReplace: Boolean = false,
        ) {
            context.enqueueUniqueIfNotScheduled<CloudSyncWork>(
                request = OneTimeWorkRequestBuilder<CloudSyncWork>()
                    .setInputData(
                        Data.Builder().apply {
                            putBoolean(ArgForceReplace, forceReplace)
                        }.build(),
                    )
                    .build(),
            )
        }
    }

    override suspend fun doWork(): Result {
        try {
            val forceReplace = inputData.getBoolean(ArgForceReplace, false)
            val cloudConfig = cloudRepository.getSyncInfo().config

            // Check if local cloud config is saved
            if (cloudConfig == null) {
                publishError(CloudError.LocalAccountDoesNotExist())
                return Result.failure()
            }

            val vault = vaultRepository.getVault()
            val vaultKeys = vaultKeysRepository.getVaultKeys(vault.id)
            val vaultHashes = vaultKeysRepository.generateVaultHashes(
                seedHex = securityRepository.getSeed().seedHex,
                vaultId = vault.id,
            )

            val cloudService = cloudServiceProvider.provide(cloudConfig)

            cloudRepository.setSyncStatus(CloudSyncStatus.Syncing)

            val result = cloudService.sync(
                config = cloudConfig,
                request = VaultSyncRequest(
                    deviceId = device.uniqueId(),
                    deviceName = device.name(),
                    seedHashHex = vaultHashes.seedHashHex,
                    vaultId = vault.id,
                    vaultCreatedAt = vault.createdAt,
                    vaultUpdatedAt = vault.updatedAt,
                ),
                mergeVaultContent = { cloudBackupContent ->
                    if (cloudBackupContent == null || forceReplace) {
                        // Push local backup
                        val localBackup = backupRepository.createVaultBackup(vaultId = vault.id, includeDeleted = true)
                        val localBackupEncrypted = backupRepository.encryptVaultBackup(localBackup)

                        VaultMergeResult.Success(
                            backupContent = backupRepository.serializeVaultBackup(localBackupEncrypted),
                            backupUpdatedAt = localBackupEncrypted.vaultUpdatedAt,
                            schemaVersion = localBackupEncrypted.schemaVersion,
                        )
                    } else {
                        // Merge local backup with cloud backup
                        val cloudBackupEncrypted = runSafely { backupRepository.readVaultBackup(cloudBackupContent) }.getOrElse {
                            if (it is InvalidSchemaVersionException) {
                                return@sync VaultMergeResult.Failure(
                                    CloudError.InvalidSchemaVersion(
                                        cause = it,
                                        backupSchemaVersion = it.backupSchemaVersion,
                                        supportedSchemaVersion = VaultBackup.CurrentSchema,
                                    ),
                                )
                            } else {
                                return@sync VaultMergeResult.Failure(CloudError.FileParsing(it))
                            }
                        }

                        if (isEligible(cloudDeviceId = cloudBackupEncrypted.originDeviceId).not()) {
                            publishError(CloudError.MultiDeviceSyncNotAvailable())
                            return@sync VaultMergeResult.Failure(CloudError.MultiDeviceSyncNotAvailable())
                        }

                        // Try to decrypt cloud backup -> quickly fail if local keys are invalid
                        runSafely {
                            vaultCryptoScope.withVaultCipher(vaultKeys) {
                                decryptWithExternalKey(cloudBackupEncrypted.encryption!!.reference.decodeBase64())
                            }
                        }.onFailure {
                            // When cloud backup is encrypted with different key -> throw an error
                            publishError(CloudError.WrongBackupPassword(it))
                            return@sync VaultMergeResult.Failure(CloudError.WrongBackupPassword(it))
                        }

                        // Decrypt cloud backup
                        val cloudBackup = backupRepository.decryptVaultBackup(
                            vaultBackup = cloudBackupEncrypted,
                            vaultKeys = vaultKeys,
                        )

                        // Create local backup
                        val localBackup = backupRepository.createVaultBackup(vaultId = vault.id, includeDeleted = true)

                        val cloudMerge = cloudMerger.merge(
                            local = localBackup,
                            cloud = cloudBackup,
                        )

                        loginsRepository.executeCloudMerge(cloudMerge.logins)
                        tagsRepository.executeCloudMerge(cloudMerge.tags)

                        deletedItemsRepository.clearAll(vault.id)
                        deletedItemsRepository.saveDeletedItems(cloudMerge.deletedItems)

                        // Create new local backup
                        val newBackup = backupRepository.createVaultBackup(vaultId = vault.id, includeDeleted = true)
                        val newBackupEncrypted = backupRepository.encryptVaultBackup(newBackup)

                        VaultMergeResult.Success(
                            backupContent = backupRepository.serializeVaultBackup(newBackupEncrypted),
                            backupUpdatedAt = newBackupEncrypted.vaultUpdatedAt,
                            schemaVersion = newBackupEncrypted.schemaVersion,
                        )
                    }
                },
            )

            when (result) {
                is CloudResult.Success -> publishSuccess()
                is CloudResult.Failure -> publishError(result.error)
            }
        } catch (e: Exception) {
            publishError(CloudError.Unknown(e))
        }

        return Result.success()
    }

    private suspend fun isEligible(cloudDeviceId: String): Boolean {
        return device.uniqueId() == cloudDeviceId || purchasesRepository.getSubscriptionPlan().entitlements.multiDeviceSync
    }

    private suspend fun publishSuccess() {
        cloudRepository.setSyncLastTime(timeProvider.currentTimeUtc())
        cloudRepository.setSyncStatus(CloudSyncStatus.Synced)
    }

    private suspend fun publishError(
        type: CloudError,
    ) {
        CrashlyticsInstance.logException(type.cause)

        cloudRepository.setSyncStatus(
            CloudSyncStatus.Error(
                error = type,
            ),
        )
    }
}