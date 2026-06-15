/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.services.common

import com.twofasapp.core.common.logger.Flog
import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.cloud.domain.CloudFileInfo
import com.twofasapp.data.cloud.domain.CloudResult
import com.twofasapp.data.cloud.domain.VaultMergeResult
import com.twofasapp.data.cloud.domain.VaultSyncRequest
import com.twofasapp.data.cloud.exceptions.CloudError
import com.twofasapp.data.cloud.exceptions.CloudException
import com.twofasapp.data.cloud.services.CloudService
import com.twofasapp.data.cloud.services.common.model.CloudIndexBackupJson
import com.twofasapp.data.cloud.services.common.model.CloudIndexJson
import java.net.UnknownServiceException

internal abstract class BackupCloudService<C : CloudConfig>(
    private val storage: BackupStorage<C>,
) : CloudService {

    private data class BackupFileMetadata(
        val deviceId: String,
        val updatedAt: Long,
    )

    protected abstract fun toFileInfo(backup: CloudIndexBackupJson): CloudFileInfo

    @Suppress("UNCHECKED_CAST")
    private fun CloudConfig.typed(): C = this as C

    private fun filename(vaultId: String): String = "${vaultId}_v1.2faspass"

    override suspend fun connect(config: CloudConfig): CloudResult {
        val typed = config.typed()
        return try {
            storage.testConnection(typed)
            storage.getIndex(typed)
            CloudResult.Success
        } catch (e: UnknownServiceException) {
            CloudResult.Failure(CloudError.CleartextNotPermitted(e))
        } catch (e: Exception) {
            CloudResult.Failure(CloudError.AuthenticationError(e))
        }
    }

    override suspend fun fetchFiles(config: CloudConfig): List<CloudFileInfo> =
        storage.getIndex(config.typed()).backups.map(::toFileInfo)

    override suspend fun fetchFile(config: CloudConfig, info: CloudFileInfo): String =
        storage.getFile(config.typed(), filename(info.vaultId))
            ?: throw RuntimeException("File not found!")

    override suspend fun sync(
        config: CloudConfig,
        request: VaultSyncRequest,
        mergeVaultContent: suspend (String?) -> VaultMergeResult,
    ): CloudResult {
        val typed = config.typed()
        return try {
            val (index, metadata) = findBackupFile(typed, request)

            when {
                metadata == null -> {
                    Flog.d("GetFile <- Metadata NOT found in \"index.2faspass\"!")
                    mergeAndPut(typed, index, request, mergeVaultContent(null))
                }

                metadata.updatedAt == request.vaultUpdatedAt && metadata.deviceId == request.deviceId -> {
                    Flog.d("GetFile <- Backup is up-to-date!")
                    CloudResult.Success
                }

                else -> {
                    Flog.d("GetFile <- Metadata found in \"index.2faspass\"!")
                    Flog.d("GetFile <- Get backup content...")
                    val backupContent = storage.getFile(typed, filename(request.vaultId))
                    mergeAndPut(typed, index, request, mergeVaultContent(backupContent))
                }
            }
        } catch (e: CloudException) {
            CloudResult.Failure(e.error)
        } catch (e: UnknownServiceException) {
            CloudResult.Failure(CloudError.CleartextNotPermitted(e))
        } catch (e: Exception) {
            CloudResult.Failure(CloudError.Unknown(e))
        }
    }

    override suspend fun disconnect() = Unit

    private suspend fun findBackupFile(
        config: C,
        request: VaultSyncRequest,
    ): Pair<CloudIndexJson, BackupFileMetadata?> {
        Flog.d("GetFile <- Starting...")
        Flog.d("GetFile <- Looking for \"${filename(request.vaultId)}\" metadata in \"index.2faspass\"")

        val index = storage.getIndex(config)
        val backup = index.backups.find {
            it.seedHashHex == request.seedHashHex && it.vaultId == request.vaultId
        }
        return index to backup?.let {
            BackupFileMetadata(deviceId = it.deviceId, updatedAt = it.vaultUpdatedAt)
        }
    }

    private suspend fun mergeAndPut(
        config: C,
        index: CloudIndexJson,
        request: VaultSyncRequest,
        mergeResult: VaultMergeResult,
    ): CloudResult = when (mergeResult) {
        is VaultMergeResult.Success -> {
            putBackupFile(config, index, request, mergeResult)
            CloudResult.Success
        }
        is VaultMergeResult.Failure -> CloudResult.Failure(mergeResult.error)
    }

    private suspend fun putBackupFile(
        config: C,
        index: CloudIndexJson,
        request: VaultSyncRequest,
        mergeResult: VaultMergeResult.Success,
    ) {
        val name = filename(request.vaultId)
        Flog.d("UpdateFile -> Starting...")
        Flog.d("UpdateFile -> Obtaining lock...")

        if (storage.obtainLock(config).not()) {
            Flog.d("UpdateFile -> Index is locked!")
            throw CloudException(CloudError.FileIsLocked())
        }

        Flog.d("UpdateFile -> Lock obtained!")
        Flog.d("UpdateFile -> Put .tmp file")
        Flog.d("UpdateFile -> ${mergeResult.backupContent}")
        storage.putFile(config, "$name.tmp", mergeResult.backupContent)

        Flog.d("UpdateFile -> Move .tmp file to final destination")
        storage.moveFile(config, "$name.tmp", name)

        Flog.d("UpdateFile -> Update index")
        storage.putIndex(
            config,
            index.copy(
                backups = index.backups
                    .filterNot { it.vaultId == request.vaultId && it.seedHashHex == request.seedHashHex }
                    .plus(
                        CloudIndexBackupJson(
                            deviceId = request.deviceId,
                            deviceName = request.deviceName,
                            seedHashHex = request.seedHashHex,
                            vaultId = request.vaultId,
                            vaultCreatedAt = request.vaultCreatedAt,
                            vaultUpdatedAt = mergeResult.backupUpdatedAt,
                            schemaVersion = mergeResult.schemaVersion,
                        ),
                    ),
            ),
        )

        Flog.d("UpdateFile -> Release lock")
        storage.releaseLock(config)
        Flog.d("UpdateFile -> \"$name\" updated successfully!")
    }
}