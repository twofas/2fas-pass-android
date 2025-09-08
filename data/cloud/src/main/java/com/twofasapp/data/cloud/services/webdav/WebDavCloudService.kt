/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.services.webdav

import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.cloud.domain.CloudFileInfo
import com.twofasapp.data.cloud.domain.CloudResult
import com.twofasapp.data.cloud.domain.VaultMergeResult
import com.twofasapp.data.cloud.domain.VaultSyncRequest
import com.twofasapp.data.cloud.exceptions.CloudError
import com.twofasapp.data.cloud.exceptions.CloudException
import com.twofasapp.data.cloud.services.CloudService
import com.twofasapp.data.cloud.services.webdav.model.WebDavIndexBackupJson
import com.twofasapp.data.cloud.services.webdav.model.WebDavIndexJson
import timber.log.Timber
import java.net.UnknownServiceException
import java.time.Instant

internal class WebDavCloudService(
    private val webDavClient: WebDavClient,
) : CloudService {

    private data class BackupFileMetadata(
        val name: String,
        val deviceId: String,
        val deviceName: String,
        val updatedAt: Long,
    )

    private fun generateFilename(request: VaultSyncRequest): String {
        return "${request.vaultId}_v1.2faspass"
    }

    override suspend fun connect(config: CloudConfig): CloudResult {
        if (config !is CloudConfig.WebDav) {
            return CloudResult.Failure(CloudError.Unknown())
        }

        return try {
            webDavClient.getIndex(config)
            CloudResult.Success
        } catch (e: UnknownServiceException) {
            CloudResult.Failure(CloudError.CleartextNotPermitted(e))
        } catch (e: Exception) {
            CloudResult.Failure(CloudError.AuthenticationError(e))
        }
    }

    override suspend fun fetchFiles(config: CloudConfig): List<CloudFileInfo> {
        if (config !is CloudConfig.WebDav) {
            return emptyList()
        }

        val index = webDavClient.getIndex(config)

        return index.backups.map {
            CloudFileInfo.WebDav(
                deviceId = it.deviceId,
                deviceName = it.deviceName,
                seedHashHex = it.seedHashHex,
                vaultId = it.vaultId,
                vaultCreatedAt = Instant.ofEpochMilli(it.vaultCreatedAt),
                vaultUpdatedAt = Instant.ofEpochMilli(it.vaultUpdatedAt),
                schemaVersion = it.schemaVersion,
            )
        }
    }

    override suspend fun fetchFile(config: CloudConfig, info: CloudFileInfo): String {
        if (config !is CloudConfig.WebDav || info !is CloudFileInfo.WebDav) {
            throw RuntimeException("Invalid config!")
        }

        return webDavClient.getFile(
            config = config,
            filename = generateFilename(
                request = VaultSyncRequest(
                    deviceId = info.deviceId,
                    deviceName = info.deviceName,
                    seedHashHex = info.seedHashHex,
                    vaultId = info.vaultId,
                    vaultCreatedAt = info.vaultCreatedAt.toEpochMilli(),
                    vaultUpdatedAt = info.vaultUpdatedAt.toEpochMilli(),
                ),
            ),
        ) ?: throw RuntimeException("File not found!")
    }

    override suspend fun sync(
        config: CloudConfig,
        request: VaultSyncRequest,
        mergeVaultContent: suspend (String?) -> VaultMergeResult,
    ): CloudResult {
        if (config !is CloudConfig.WebDav) {
            return CloudResult.Failure(CloudError.Unknown())
        }

        return try {
            val (index, backupFileMetadata) = findBackupFile(
                config = config,
                request = request,
            )

            when {
                backupFileMetadata == null -> {
                    Timber.d("GetFile <- Metadata NOT found in \"index.2faspass\"!")
                    when (val mergeResult = mergeVaultContent(null)) {
                        is VaultMergeResult.Success -> {
                            putBackupFile(
                                config = config,
                                index = index,
                                request = request,
                                mergeResult = mergeResult,
                            )
                            CloudResult.Success
                        }

                        is VaultMergeResult.Failure -> {
                            CloudResult.Failure(mergeResult.error)
                        }
                    }
                }

                backupFileMetadata.updatedAt == request.vaultUpdatedAt && backupFileMetadata.deviceId == request.deviceId -> {
                    Timber.d("GetFile <- Backup is up-to-date!")
                    CloudResult.Success
                }

                else -> {
                    Timber.d("GetFile <- Metadata found in \"index.2faspass\"!")

                    val backupFileContent = getBackupFileContent(
                        config = config,
                        request = request,
                    )

                    when (val mergeResult = mergeVaultContent(backupFileContent)) {
                        is VaultMergeResult.Success -> {
                            putBackupFile(
                                config = config,
                                index = index,
                                request = request,
                                mergeResult = mergeResult,
                            )
                            CloudResult.Success
                        }

                        is VaultMergeResult.Failure -> {
                            CloudResult.Failure(mergeResult.error)
                        }
                    }
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

    private suspend fun findBackupFile(
        config: CloudConfig.WebDav,
        request: VaultSyncRequest,
    ): Pair<WebDavIndexJson, BackupFileMetadata?> {
        val filename = generateFilename(request)

        Timber.d("GetFile <- Starting...")
        Timber.d("GetFile <- Looking for \"${filename}\" metadata in \"index.2faspass\"")

        val index = webDavClient.getIndex(config)

        val resource = index.backups.find { it.seedHashHex == request.seedHashHex && it.vaultId == request.vaultId }

        return Pair(
            first = index,
            second = resource?.let {
                BackupFileMetadata(
                    name = generateFilename(request),
                    deviceId = it.deviceId,
                    deviceName = it.deviceName,
                    updatedAt = it.vaultUpdatedAt,
                )
            },
        )
    }

    private suspend fun getBackupFileContent(
        config: CloudConfig.WebDav,
        request: VaultSyncRequest,
    ): String? {
        Timber.d("GetFile <- Get backup content...")

        return webDavClient.getFile(
            config = config,
            filename = generateFilename(request),
        )
    }

    private suspend fun putBackupFile(
        config: CloudConfig.WebDav,
        index: WebDavIndexJson,
        request: VaultSyncRequest,
        mergeResult: VaultMergeResult.Success,
    ) {
        Timber.d("UpdateFile -> Starting...")
        Timber.d("UpdateFile -> Obtaining lock...")

        if (webDavClient.obtainLock(config).not()) {
            Timber.d("UpdateFile -> Index is locked!")
            throw CloudException(CloudError.FileIsLocked())
        }

        Timber.d("UpdateFile -> Lock obtained!")

        // Upload backup to tmp file
        Timber.d("UpdateFile -> Put .tmp file")
        Timber.d("UpdateFile -> ${mergeResult.backupContent}")

        webDavClient.putFile(
            config = config,
            filename = "${generateFilename(request)}.tmp",
            content = mergeResult.backupContent,
        )

        // Move from tmp to final destination
        Timber.d("UpdateFile -> Move .tmp file to final destination")
        webDavClient.moveFile(
            config = config,
            source = "${generateFilename(request)}.tmp",
            destination = generateFilename(request),
        )

        // Update index
        Timber.d("UpdateFile -> Update index")

        webDavClient.putIndex(
            config = config,
            index = index.copy(
                backups = index.backups
                    .filterNot { it.vaultId == request.vaultId && it.seedHashHex == request.seedHashHex }
                    .plus(
                        WebDavIndexBackupJson(
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

        Timber.d("UpdateFile -> Release lock")
        webDavClient.releaseLock(config)

        Timber.d("UpdateFile -> \"${generateFilename(request)}\" updated successfully!")
    }

    override suspend fun disconnect() = Unit
}