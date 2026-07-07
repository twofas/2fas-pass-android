/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.crypto.decrypt
import com.twofasapp.core.common.crypto.encrypt
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.core.common.ktx.encodeBase64
import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.cloud.domain.CloudConnection
import com.twofasapp.data.cloud.domain.CloudSyncStatus
import com.twofasapp.data.cloud.exceptions.CloudError
import com.twofasapp.data.main.DerivedKeysRepository
import com.twofasapp.data.main.local.model.CloudConfigEntity
import com.twofasapp.data.main.local.model.CloudConnectionEntity
import kotlinx.serialization.json.Json

class CloudConfigMapper(
    private val json: Json,
    private val derivedKeysRepository: DerivedKeysRepository,
) {
    suspend fun mapToDomain(entity: CloudConfigEntity): CloudConfig {
        val connectionJson = decrypt(metadataKey(), EncryptedBytes(entity.connection.decodeBase64())).decodeToString()
        val connectionEntity = json.decodeFromString(CloudConnectionEntity.Serializer, connectionJson)
        return CloudConfig(
            id = entity.id,
            syncedAt = entity.syncedAt,
            status = mapStatus(entity.status, entity.errorCode),
            connection = mapConnectionToDomain(connectionEntity),
        )
    }

    suspend fun mapToEntity(domain: CloudConfig, createdAt: Long): CloudConfigEntity {
        val connectionJson = json.encodeToString(CloudConnectionEntity.Serializer, mapConnectionToEntity(domain.connection))
        return CloudConfigEntity(
            id = domain.id,
            createdAt = createdAt,
            syncedAt = domain.syncedAt,
            status = statusName(domain.status),
            errorCode = errorCode(domain.status),
            connection = encrypt(metadataKey(), connectionJson.toByteArray()).bytes.encodeBase64(),
        )
    }

    private suspend fun metadataKey(): ByteArray {
        return derivedKeysRepository.getDerivedKeys().metadata ?: throw IllegalStateException("Metadata key is not available")
    }

    fun statusName(status: CloudSyncStatus): String = when (status) {
        CloudSyncStatus.Idle -> StatusIdle
        CloudSyncStatus.Syncing -> StatusSyncing
        CloudSyncStatus.Synced -> StatusSynced
        is CloudSyncStatus.Error -> StatusError
    }

    fun errorCode(status: CloudSyncStatus): String? = when (status) {
        is CloudSyncStatus.Error -> errorCodeOf(status.error)
        else -> null
    }

    private fun mapStatus(name: String, errorCode: String?): CloudSyncStatus = when (name) {
        StatusSyncing -> CloudSyncStatus.Syncing
        StatusSynced -> CloudSyncStatus.Synced
        StatusError -> CloudSyncStatus.Error(errorFromCode(errorCode))
        else -> CloudSyncStatus.Idle
    }

    private fun mapConnectionToDomain(entity: CloudConnectionEntity): CloudConnection = when (entity) {
        is CloudConnectionEntity.GoogleDrive -> CloudConnection.GoogleDrive(
            accountId = entity.accountId,
            credentialType = entity.credentialType,
        )
        is CloudConnectionEntity.WebDav -> CloudConnection.WebDav(
            url = entity.url,
            username = entity.username,
            password = entity.password,
            allowUntrustedCertificate = entity.allowUntrustedCertificate,
        )
        is CloudConnectionEntity.S3 -> CloudConnection.S3(
            endpoint = entity.endpoint,
            region = entity.region,
            bucket = entity.bucket,
            accessKeyId = entity.accessKeyId,
            secretAccessKey = entity.secretAccessKey,
            allowUntrustedCertificate = entity.allowUntrustedCertificate,
        )
    }

    private fun mapConnectionToEntity(connection: CloudConnection): CloudConnectionEntity = when (connection) {
        is CloudConnection.GoogleDrive -> CloudConnectionEntity.GoogleDrive(
            accountId = connection.accountId,
            credentialType = connection.credentialType,
        )
        is CloudConnection.WebDav -> CloudConnectionEntity.WebDav(
            url = connection.url,
            username = connection.username,
            password = connection.password,
            allowUntrustedCertificate = connection.allowUntrustedCertificate,
        )
        is CloudConnection.S3 -> CloudConnectionEntity.S3(
            endpoint = connection.endpoint,
            region = connection.region,
            bucket = connection.bucket,
            accessKeyId = connection.accessKeyId,
            secretAccessKey = connection.secretAccessKey,
            allowUntrustedCertificate = connection.allowUntrustedCertificate,
        )
    }

    private fun errorCodeOf(error: CloudError): String = when (error) {
        is CloudError.Unknown -> "Unknown"
        is CloudError.AuthenticationError -> "AuthenticationError"
        is CloudError.NoNetwork -> "NoNetwork"
        is CloudError.GetFile -> "GetFile"
        is CloudError.CreateFile -> "CreateFile"
        is CloudError.UpdateFile -> "UpdateFile"
        is CloudError.FileParsing -> "FileParsing"
        is CloudError.LocalAccountDoesNotExist -> "LocalAccountDoesNotExist"
        is CloudError.NotAuthorized -> "NotAuthorized"
        is CloudError.WrongBackupPassword -> "WrongBackupPassword"
        is CloudError.FileIsLocked -> "FileIsLocked"
        is CloudError.MultiDeviceSyncNotAvailable -> "MultiDeviceSyncNotAvailable"
        is CloudError.CleartextNotPermitted -> "CleartextNotPermitted"
        is CloudError.InvalidSchemaVersion -> "InvalidSchemaVersion"
    }

    private fun errorFromCode(code: String?): CloudError = when (code) {
        "AuthenticationError" -> CloudError.AuthenticationError()
        "NoNetwork" -> CloudError.NoNetwork()
        "GetFile" -> CloudError.GetFile()
        "CreateFile" -> CloudError.CreateFile()
        "UpdateFile" -> CloudError.UpdateFile()
        "FileParsing" -> CloudError.FileParsing()
        "LocalAccountDoesNotExist" -> CloudError.LocalAccountDoesNotExist()
        "NotAuthorized" -> CloudError.NotAuthorized(intent = null)
        "WrongBackupPassword" -> CloudError.WrongBackupPassword()
        "FileIsLocked" -> CloudError.FileIsLocked()
        "MultiDeviceSyncNotAvailable" -> CloudError.MultiDeviceSyncNotAvailable()
        "CleartextNotPermitted" -> CloudError.CleartextNotPermitted()
        "InvalidSchemaVersion" -> CloudError.InvalidSchemaVersion(backupSchemaVersion = 0, supportedSchemaVersion = 0)
        else -> CloudError.Unknown()
    }

    companion object {
        const val StatusIdle = "Idle"
        const val StatusSyncing = "Syncing"
        const val StatusSynced = "Synced"
        const val StatusError = "Error"
    }
}