/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
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
import com.twofasapp.core.common.auth.AuthStatusTracker
import com.twofasapp.core.common.build.Device
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.core.common.logger.Flog
import com.twofasapp.core.common.services.CrashlyticsInstance
import com.twofasapp.core.common.time.TimeProvider
import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.cloud.domain.CloudConnection
import com.twofasapp.data.cloud.domain.CloudResult
import com.twofasapp.data.cloud.domain.CloudSyncStatus
import com.twofasapp.data.cloud.domain.VaultMergeResult
import com.twofasapp.data.cloud.domain.VaultSyncRequest
import com.twofasapp.data.cloud.exceptions.CloudError
import com.twofasapp.data.cloud.services.CloudServiceProvider
import com.twofasapp.data.main.BackupRepository
import com.twofasapp.data.main.CloudRepository
import com.twofasapp.data.main.DeletedItemsRepository
import com.twofasapp.data.main.ItemsRepository
import com.twofasapp.data.main.SecurityRepository
import com.twofasapp.data.main.TagsRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.VaultKeysRepository
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.main.domain.CloudMerger
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
    private val authStatusTracker: AuthStatusTracker by inject()
    private val vaultRepository: VaultsRepository by inject()
    private val vaultKeysRepository: VaultKeysRepository by inject()
    private val backupRepository: BackupRepository by inject()
    private val itemsRepository: ItemsRepository by inject()
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
        val forceReplace = inputData.getBoolean(ArgForceReplace, false)

        if (authStatusTracker.isAuthenticated().not()) {
            logBoth("Vault is locked, skipping cloud sync")
            return Result.failure()
        }

        val configs = cloudRepository.getConfigs()

        if (configs.isEmpty()) {
            logBoth("No local cloud config")
            return Result.failure()
        }

        // Pass 1: sync providers ordered by freshness (newest lastSuccessfulSyncTime first).
        // Newest first means local DB picks up the most up-to-date items as early as possible,
        // minimizing the number of providers that finish pass 1 missing items they would later
        // discover from another provider. Tie-break by provider kind (GoogleDrive → WebDav → S3)
        // so the order is deterministic on the very first sync when all timestamps are 0.
        val freshestFirst = configs.sortedWith(
            compareByDescending<CloudConfig> { it.syncedAt }
                .thenBy { it.providerRank() },
        )
        val successfulProviders = mutableListOf<CloudConfig>()
        var localDataChanged = false

        logBoth("Pass 1 starting with ${freshestFirst.size} provider(s): ${freshestFirst.joinToString { it.describe() }}")

        for ((index, config) in freshestFirst.withIndex()) {
            logBoth("── Pass 1 [${index + 1}/${freshestFirst.size}] ${config.describe()} ──")
            // vault.updatedAt before this provider's sync — if it grows, that provider brought
            // in new items and previously-synced providers are now stale. Read defensively so a
            // transient DB hiccup never aborts the chain.
            val updatedAtBefore = runSafely { vaultRepository.getVault().updatedAt }.getOrElse { -1L }
            val ok = runSyncForProvider(config, forceReplace = forceReplace)
            if (ok) successfulProviders += config
            val updatedAtAfter = runSafely { vaultRepository.getVault().updatedAt }.getOrElse { updatedAtBefore }
            if (updatedAtBefore >= 0 && updatedAtAfter > updatedAtBefore) {
                localDataChanged = true
            }
        }

        logBoth("Pass 1 complete (successful=${successfulProviders.size}/${freshestFirst.size}, localDataChanged=$localDataChanged)")

        // Pass 2: if any earlier provider's merge pulled in new items, the providers that ran
        // before that point did not see those items. Re-sync them so every remote ends up with
        // the same union. Drop the last successful one — it ran with the final local state and
        // is already current. For providers that happen to already be current, BackupCloudService
        // short-circuits at the index check (no file upload), so this pass is cheap.
        if (localDataChanged && successfulProviders.size >= 2) {
            val staleProviders = successfulProviders.dropLast(1)
            logBoth("Pass 2 starting, reconciling ${staleProviders.size} provider(s): ${staleProviders.joinToString { it.describe() }}")
            for ((index, config) in staleProviders.withIndex()) {
                logBoth("── Pass 2 [${index + 1}/${staleProviders.size}] ${config.describe()} ──")
                runSyncForProvider(config, forceReplace = false)
            }
            logBoth("Pass 2 complete")
        } else {
            logBoth("Pass 2 skipped (localDataChanged=$localDataChanged, successful=${successfulProviders.size})")
        }

        logBoth("Work finished")
        return Result.success()
    }

    // Emits the same message to both logcat (immediate visibility while debugging) and the
    // persistent log (shipped in user-submitted log dumps). Use sparingly — only for milestones
    // we want to read months later in a customer report.
    private fun logBoth(message: String) {
        Flog.tag("CloudSync").d(message)
        Flog.persist(tag = "CloudSync", message = message)
    }

    private fun CloudConfig.providerRank(): Int = when (connection) {
        is CloudConnection.GoogleDrive -> 0
        is CloudConnection.WebDav -> 1
        is CloudConnection.S3 -> 2
    }

    private fun CloudConfig.describe(): String {
        val type = when (connection) {
            is CloudConnection.GoogleDrive -> "GoogleDrive"
            is CloudConnection.WebDav -> "WebDav"
            is CloudConnection.S3 -> "S3"
        }
        return "$type#${id.take(8)}"
    }

    // Runs one provider's sync end-to-end. Always publishes a terminal status (Synced or Error)
    // so the DB row never gets stuck on Syncing, and never throws — failures here must not stop
    // the surrounding loop from continuing with the next provider. CancellationException still
    // propagates via runSafely so WorkManager can honor cancellation. Returns true on success.
    private suspend fun runSyncForProvider(
        config: CloudConfig,
        forceReplace: Boolean,
    ): Boolean {
        publishStatus(config.id, CloudSyncStatus.Syncing)
        return runSafely { syncConfig(config, forceReplace) }
            .fold(
                onSuccess = { result ->
                    when (result) {
                        is CloudResult.Success -> {
                            publishSuccess(config.id)
                            true
                        }
                        is CloudResult.Failure -> {
                            publishError(config.id, result.error)
                            false
                        }
                    }
                },
                onFailure = { error ->
                    publishError(config.id, CloudError.Unknown(error))
                    false
                },
            )
    }

    private suspend fun publishStatus(id: String, status: CloudSyncStatus) {
        runSafely { cloudRepository.setSyncStatus(id, status) }
            .onFailure { Flog.persist(tag = "CloudSync", message = "Failed to publish status for $id: ${it.message}") }
    }

    private suspend fun syncConfig(
        config: CloudConfig,
        forceReplace: Boolean,
    ): CloudResult {
        logBoth("Started ${config.describe()}, forceReplace=$forceReplace")

        val vault = vaultRepository.getVault()
        val vaultKeys = vaultKeysRepository.getVaultKeys(vault.id)
        val vaultHashes = vaultKeysRepository.generateVaultHashes(
            seedHex = securityRepository.getSeed().seedHex,
            vaultId = vault.id,
        )

        val cloudService = cloudServiceProvider.provide(config.connection)

        return cloudService.sync(
            connection = config.connection,
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
                    logBoth("Pushing local backup (cloudEmpty=${cloudBackupContent == null}, forceReplace=$forceReplace)")
                    val localBackup = backupRepository.createVaultBackup(vaultId = vault.id, includeDeleted = true, decryptSecretFields = false)
                    val localBackupEncrypted = backupRepository.encryptVaultBackup(localBackup)

                    VaultMergeResult.Success(
                        backupContent = backupRepository.serializeVaultBackup(localBackupEncrypted),
                        backupUpdatedAt = localBackupEncrypted.vaultUpdatedAt,
                        schemaVersion = localBackupEncrypted.schemaVersion,
                    )
                } else {
                    logBoth("Merging local with cloud backup")
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
                        logBoth("Multi-device sync not available")
                        return@sync VaultMergeResult.Failure(CloudError.MultiDeviceSyncNotAvailable())
                    }

                    runSafely {
                        vaultCryptoScope.withVaultCipher(vaultKeys) {
                            decryptWithExternalKey(cloudBackupEncrypted.encryption!!.reference.decodeBase64())
                        }
                    }.onFailure {
                        logBoth("Wrong backup password")
                        return@sync VaultMergeResult.Failure(CloudError.WrongBackupPassword(it))
                    }

                    val cloudBackup = backupRepository.decryptVaultBackup(
                        vaultBackup = cloudBackupEncrypted,
                        vaultKeys = vaultKeys,
                        decryptSecretFields = false,
                    )

                    val localBackup = backupRepository.createVaultBackup(vaultId = vault.id, includeDeleted = true, decryptSecretFields = false)

                    val cloudMerge = cloudMerger.merge(
                        local = localBackup,
                        cloud = cloudBackup,
                    )

                    logBoth("Merge complete, applying changes (add=${cloudMerge.items.toAdd.size}, update=${cloudMerge.items.toUpdate.size}, delete=${cloudMerge.items.toDelete.size})")
                    itemsRepository.executeCloudMerge(cloudMerge.items)
                    tagsRepository.executeCloudMerge(cloudMerge.tags)

                    deletedItemsRepository.clearAll(vault.id)
                    deletedItemsRepository.saveDeletedItems(cloudMerge.deletedItems)

                    val newBackup = backupRepository.createVaultBackup(vaultId = vault.id, includeDeleted = true, decryptSecretFields = false)
                    val newBackupEncrypted = backupRepository.encryptVaultBackup(newBackup)

                    VaultMergeResult.Success(
                        backupContent = backupRepository.serializeVaultBackup(newBackupEncrypted),
                        backupUpdatedAt = newBackupEncrypted.vaultUpdatedAt,
                        schemaVersion = newBackupEncrypted.schemaVersion,
                    )
                }
            },
        )
    }

    private suspend fun isEligible(cloudDeviceId: String): Boolean {
        return device.uniqueId() == cloudDeviceId || purchasesRepository.getSubscriptionPlan().entitlements.multiDeviceSync
    }

    // publishSuccess / publishError must never throw (except for cancellation) — they finalize
    // per-provider state inside the per-provider runSafely. A failed DB write here must not
    // abort the surrounding loop.
    private suspend fun publishSuccess(id: String) {
        logBoth("Success (id=$id)")
        runSafely { cloudRepository.setSyncLastTime(id, timeProvider.currentTimeUtc()) }
            .onFailure { Flog.persist(tag = "CloudSync", message = "Failed to persist lastSyncTime for $id: ${it.message}") }
        publishStatus(id, CloudSyncStatus.Synced)
    }

    private suspend fun publishError(
        id: String,
        type: CloudError,
    ) {
        logBoth("Error (id=$id): ${type::class.simpleName}")
        Flog.persist(tag = "CloudSync", throwable = type.cause)
        Flog.e(type.cause)

        runSafely { CrashlyticsInstance.logException(type.cause) }
        publishStatus(id, CloudSyncStatus.Error(error = type))
    }
}