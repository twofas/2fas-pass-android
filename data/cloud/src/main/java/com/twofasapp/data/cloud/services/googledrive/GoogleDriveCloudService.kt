/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.services.googledrive

import android.accounts.Account
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.twofasapp.core.common.logger.Flog
import com.twofasapp.data.cloud.authenticate.CloudServiceType
import com.twofasapp.data.cloud.authenticate.DefaultCloudServiceType
import com.twofasapp.data.cloud.domain.CloudConnection
import com.twofasapp.data.cloud.domain.CloudFileInfo
import com.twofasapp.data.cloud.domain.CloudResult
import com.twofasapp.data.cloud.domain.VaultMergeResult
import com.twofasapp.data.cloud.domain.VaultSyncRequest
import com.twofasapp.data.cloud.exceptions.CloudError
import com.twofasapp.data.cloud.exceptions.CloudException
import com.twofasapp.data.cloud.services.CloudService
import com.twofasapp.data.cloud.services.googledrive.GoogleDriveCloudService.BackupFileMetadata.Companion.UnknownDevice
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.Instant
import java.util.Collections

internal class GoogleDriveCloudService(
    private val context: Context,
) : CloudService {

    private data class BackupFileMetadata(
        val id: String,
        val name: String,
        val deviceId: String,
        val deviceName: String,
        val createdAt: Long,
        val updatedAt: Long,
        val schemaVersion: Int,
    ) {
        companion object {
            const val Prefix = "vault-backup"
            const val UnknownDevice = "Unknown Device"
            const val PropertyDeviceId = "deviceId"
            const val PropertyDeviceName = "deviceName"
            const val PropertyUpdatedAt = "vaultUpdatedAt"
            const val PropertyCreatedAt = "vaultCreatedAt"
            const val PropertySchemaVersion = "schemaVersion"
        }
    }

    private fun generateFilename(request: VaultSyncRequest): String {
        return "${BackupFileMetadata.Prefix}_${request.seedHashHex}_${request.vaultId}_v1.2faspass"
    }

    override suspend fun connect(connection: CloudConnection): CloudResult {
        return CloudResult.Success
    }

    override suspend fun fetchFiles(connection: CloudConnection): List<CloudFileInfo> {
        if (connection !is CloudConnection.GoogleDrive) {
            return emptyList()
        }

        return connection.drive().getAllFiles().map { file ->
            CloudFileInfo.GoogleDrive(
                fileId = file.id,
                deviceId = file.properties?.get(BackupFileMetadata.PropertyDeviceId) ?: "",
                deviceName = file.properties?.get(BackupFileMetadata.PropertyDeviceName) ?: UnknownDevice,
                seedHashHex = file.name.split("_")[1],
                vaultId = file.name.split("_")[2],
                vaultCreatedAt = Instant.ofEpochMilli(file.properties?.get(BackupFileMetadata.PropertyCreatedAt)?.toLongOrNull() ?: 0L),
                vaultUpdatedAt = Instant.ofEpochMilli(file.properties?.get(BackupFileMetadata.PropertyUpdatedAt)?.toLongOrNull() ?: 0L),
                schemaVersion = file.properties?.get(BackupFileMetadata.PropertySchemaVersion)?.toIntOrNull() ?: 1,
            )
        }
    }

    override suspend fun fetchFile(connection: CloudConnection, info: CloudFileInfo): String {
        if (connection !is CloudConnection.GoogleDrive || info !is CloudFileInfo.GoogleDrive) {
            throw RuntimeException("Invalid config!")
        }

        return connection.drive().getBackupFileContent(id = info.fileId)
    }

    override suspend fun sync(
        connection: CloudConnection,
        request: VaultSyncRequest,
        mergeVaultContent: suspend (String?) -> VaultMergeResult,
    ): CloudResult {
        if (connection !is CloudConnection.GoogleDrive) {
            return CloudResult.Failure(CloudError.Unknown())
        }

        return try {
            val drive = connection.drive()
            val requestedFilename = generateFilename(request)
            val backupFileMetadata = drive.findBackupFile(requestedFilename)

            when {
                backupFileMetadata == null -> {
                    Flog.d("GetFile <- Backup not found!")
                    when (val mergeResult = mergeVaultContent(null)) {
                        is VaultMergeResult.Success -> {
                            drive.createBackupFile(
                                deviceId = request.deviceId,
                                deviceName = request.deviceName,
                                name = requestedFilename,
                                content = mergeResult.backupContent,
                                createdAt = request.vaultCreatedAt,
                                updatedAt = mergeResult.backupUpdatedAt,
                                schemaVersion = mergeResult.schemaVersion,
                            )

                            CloudResult.Success
                        }

                        is VaultMergeResult.Failure -> {
                            CloudResult.Failure(mergeResult.error)
                        }
                    }
                }

                backupFileMetadata.updatedAt == request.vaultUpdatedAt && (backupFileMetadata.deviceId == request.deviceId || backupFileMetadata.deviceName == UnknownDevice) -> {
                    Flog.d("GetFile <- Backup is up-to-date!")
                    CloudResult.Success
                }

                else -> {
                    Flog.d("GetFile <- Backup found! (updatedAt = ${backupFileMetadata.updatedAt})")

                    val backupFileContent = drive.getBackupFileContent(backupFileMetadata.id)
                    when (val mergeResult = mergeVaultContent(backupFileContent)) {
                        is VaultMergeResult.Success -> {
                            drive.updateBackupFile(
                                deviceId = request.deviceId,
                                deviceName = request.deviceName,
                                id = backupFileMetadata.id,
                                name = backupFileMetadata.name,
                                content = mergeResult.backupContent,
                                createdAt = request.vaultCreatedAt,
                                updatedAt = mergeResult.backupUpdatedAt,
                                schemaVersion = mergeResult.schemaVersion,
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
        } catch (e: Exception) {
            CloudResult.Failure(CloudError.Unknown(e))
        }
    }

    private fun Drive.getAllFiles(): List<File> {
        return files()
            .list()
            .setFields("files(id, name, properties(${BackupFileMetadata.PropertyUpdatedAt}, ${BackupFileMetadata.PropertyDeviceName}, ${BackupFileMetadata.PropertyDeviceId}, ${BackupFileMetadata.PropertyCreatedAt}, ${BackupFileMetadata.PropertySchemaVersion}))")
            .setSpaces("appDataFolder")
            .execute()
            ?.files
            ?.filter {
                it.name.startsWith(
                    prefix = BackupFileMetadata.Prefix,
                    ignoreCase = true,
                )
            }.orEmpty()
    }

    private fun Drive.findBackupFile(filename: String): BackupFileMetadata? {
        try {
            Flog.d("GetFile <- Starting...")

            val allFiles = getAllFiles()

            Flog.d("GetFile <- Looking for \"${filename}\"...")

            return if (allFiles.isEmpty()) {
                null
            } else {
                allFiles.find { it.name == filename }?.let { file ->
                    BackupFileMetadata(
                        id = file.id,
                        name = file.name,
                        deviceId = file.properties?.get(BackupFileMetadata.PropertyDeviceId) ?: "",
                        deviceName = file.properties?.get(BackupFileMetadata.PropertyDeviceName) ?: UnknownDevice,
                        createdAt = file.properties?.get(BackupFileMetadata.PropertyCreatedAt)?.toLongOrNull() ?: 0L,
                        updatedAt = file.properties?.get(BackupFileMetadata.PropertyUpdatedAt)?.toLongOrNull() ?: 0L,
                        schemaVersion = file.properties?.get(BackupFileMetadata.PropertySchemaVersion)?.toIntOrNull() ?: 1,
                    )
                }
            }
        } catch (e: Exception) {
            throw CloudException(e.mapCommonExceptions() ?: CloudError.GetFile(e))
        }
    }

    private fun Drive.createBackupFile(
        deviceId: String,
        deviceName: String,
        name: String,
        content: String,
        createdAt: Long,
        updatedAt: Long,
        schemaVersion: Int,
    ) {
        try {
            Flog.d("CreateFile -> Starting...")

            val metadata = File()
                .setParents(listOf("appDataFolder"))
                .setMimeType("application/json")
                .setName(name)
                .setProperties(
                    mapOf(
                        BackupFileMetadata.PropertyDeviceId to deviceId,
                        BackupFileMetadata.PropertyDeviceName to deviceName,
                        BackupFileMetadata.PropertyCreatedAt to createdAt.toString(),
                        BackupFileMetadata.PropertyUpdatedAt to updatedAt.toString(),
                        BackupFileMetadata.PropertySchemaVersion to schemaVersion.toString(),
                    ),
                )

            files()
                .create(metadata, ByteArrayContent.fromString("text/plain", content))
                .execute()

            Flog.d("CreateFile -> $content")
            Flog.d("CreateFile -> \"${name}\" created successfully!")
        } catch (e: Exception) {
            throw CloudException(e.mapCommonExceptions() ?: CloudError.CreateFile(e))
        }
    }

    private fun Drive.updateBackupFile(
        deviceId: String,
        deviceName: String,
        id: String,
        name: String,
        content: String,
        createdAt: Long,
        updatedAt: Long,
        schemaVersion: Int,
    ) {
        try {
            Flog.d("UpdateFile -> Starting...")

            files()
                .update(
                    id,
                    File()
                        .setName(name)
                        .setProperties(
                            mapOf(
                                BackupFileMetadata.PropertyDeviceId to deviceId,
                                BackupFileMetadata.PropertyDeviceName to deviceName,
                                BackupFileMetadata.PropertyCreatedAt to createdAt.toString(),
                                BackupFileMetadata.PropertyUpdatedAt to updatedAt.toString(),
                                BackupFileMetadata.PropertySchemaVersion to schemaVersion.toString(),
                            ),
                        ),
                    ByteArrayContent.fromString("text/plain", content),
                )
                .execute()

            Flog.d("UpdateFile -> $content")
            Flog.d("UpdateFile -> \"${name}\" updated successfully!")
        } catch (e: Exception) {
            throw CloudException(e.mapCommonExceptions() ?: CloudError.UpdateFile(e))
        }
    }

    private fun Drive.getBackupFileContent(
        id: String,
    ): String {
        try {
            val content = files()[id]
                .executeMediaAsInputStream()
                .use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        reader.readText()
                    }
                }

            Flog.d("GetFile <- $content")

            return content
        } catch (e: Exception) {
            throw CloudException(e.mapCommonExceptions() ?: CloudError.GetFile(e))
        }
    }

    override suspend fun disconnect() {
        when (DefaultCloudServiceType) {
            CloudServiceType.GoogleDrive -> {
                CredentialManager.create(context).clearCredentialState(
                    ClearCredentialStateRequest(),
                )
            }

            CloudServiceType.LegacyGoogleDrive -> {
                GoogleSignIn.getClient(
                    context,
                    GoogleSignInOptions.Builder()
                        .requestEmail()
                        .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
                        .build(),
                ).signOut()
            }
        }
    }

    private fun CloudConnection.GoogleDrive.drive(): Drive {
        val googleAccountCredential = GoogleAccountCredential
            .usingOAuth2(context, Collections.singleton(DriveScopes.DRIVE_APPDATA))
            .apply {
                selectedAccount = Account(
                    this@drive.accountId,
                    this@drive.credentialType,
                )
            }

        return Drive.Builder(
            NetHttpTransport.Builder().build(),
            GsonFactory(),
            googleAccountCredential,
        )
            .setApplicationName("2FAS Pass")
            .build()
    }

    private fun Exception.mapCommonExceptions(): CloudError? {
        return when (this) {
            is UserRecoverableAuthIOException -> CloudError.NotAuthorized(this, intent)
            is UserRecoverableAuthException -> CloudError.NotAuthorized(this, intent)
            else -> null
        }
    }
}