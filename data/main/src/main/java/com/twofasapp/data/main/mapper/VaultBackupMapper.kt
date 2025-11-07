/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.crypto.EncryptionSpec
import com.twofasapp.core.common.domain.crypto.asDomain
import com.twofasapp.core.common.domain.crypto.asEntity
import com.twofasapp.data.main.domain.VaultBackup
import com.twofasapp.data.main.remote.model.EncryptionSpecJson
import com.twofasapp.data.main.remote.model.vaultbackup.VaultBackupV1Json
import com.twofasapp.data.main.remote.model.vaultbackup.VaultBackupV2Json
import kotlinx.serialization.json.Json

internal class VaultBackupMapper(
    private val jsonSerializer: Json,
    private val itemMapper: ItemMapper,
    private val tagMapper: TagMapper,
    private val deletedItemsMapper: DeletedItemsMapper,
) {
    fun mapToJson(domain: VaultBackup): VaultBackupV2Json {
        return with(domain) {
            VaultBackupV2Json(
                schemaVersion = VaultBackup.CurrentSchema,
                origin = VaultBackupV2Json.Origin(
                    os = originOs,
                    appVersionCode = originAppVersionCode,
                    appVersionName = originAppVersionName,
                    deviceId = originDeviceId,
                    deviceName = originDeviceName,
                ),
                encryption = encryption?.let { mapToJson(it) },
                vault = VaultBackupV2Json.Vault(
                    id = vaultId,
                    name = vaultName,
                    createdAt = vaultCreatedAt,
                    updatedAt = vaultUpdatedAt,
                    items = items?.mapNotNull { itemMapper.mapToJson(it) },
                    itemsEncrypted = itemsEncrypted,
                    tags = tags?.let { tagMapper.mapToJson(it) },
                    tagsEncrypted = tagsEncrypted,
                    deletedItems = deletedItems?.let { deletedItemsMapper.mapToJson(it) },
                    deletedItemsEncrypted = deletedItemsEncrypted,
                ),
            )
        }
    }

    fun mapToDomain(schemaVersion: Int, content: String, deviceIdFallback: String): VaultBackup {
        return when (schemaVersion) {
            1 -> mapToDomainFromV1(json = jsonSerializer.decodeFromString(VaultBackupV1Json.serializer(), content), deviceIdFallback = deviceIdFallback)
            2 -> mapToDomainFromV2(json = jsonSerializer.decodeFromString(VaultBackupV2Json.serializer(), content), deviceIdFallback = deviceIdFallback)
            else -> throw IllegalArgumentException("Unsupported schema version: $schemaVersion")
        }
    }

    private fun mapToDomainFromV1(json: VaultBackupV1Json, deviceIdFallback: String): VaultBackup {
        return with(json) {
            VaultBackup(
                schemaVersion = schemaVersion,
                originOs = origin.os,
                originAppVersionCode = origin.appVersionCode,
                originAppVersionName = origin.appVersionName,
                originDeviceId = origin.deviceId ?: deviceIdFallback,
                originDeviceName = origin.deviceName,
                vaultId = vault.id,
                vaultName = vault.name,
                vaultCreatedAt = vault.createdAt,
                vaultUpdatedAt = vault.updatedAt,
                items = vault.logins?.map { itemMapper.mapToDomainFromV1(json = it, vaultId = vault.id) },
                itemsEncrypted = vault.loginsEncrypted,
                tags = vault.tags?.map { tagMapper.mapToDomain(json = it, vaultId = vault.id) },
                tagsEncrypted = vault.tagsEncrypted,
                deletedItems = vault.deletedItems?.map { deletedItemsMapper.mapToDomain(json = it, vault.id) },
                deletedItemsEncrypted = vault.deletedItemsEncrypted,
                encryption = encryption?.let { mapToDomain(it) },
            )
        }
    }

    private fun mapToDomainFromV2(json: VaultBackupV2Json, deviceIdFallback: String): VaultBackup {
        return with(json) {
            VaultBackup(
                schemaVersion = schemaVersion,
                originOs = origin.os,
                originAppVersionCode = origin.appVersionCode,
                originAppVersionName = origin.appVersionName,
                originDeviceId = origin.deviceId ?: deviceIdFallback,
                originDeviceName = origin.deviceName,
                vaultId = vault.id,
                vaultName = vault.name,
                vaultCreatedAt = vault.createdAt,
                vaultUpdatedAt = vault.updatedAt,
                items = vault.items?.map {
                    itemMapper.mapToDomain(
                        json = it,
                        vaultId = vault.id,
                        tagIds = it.tags,
                        hasSecretFieldsEncrypted = false,
                    )
                },
                itemsEncrypted = vault.itemsEncrypted,
                tags = vault.tags?.map { tagMapper.mapToDomain(json = it, vaultId = vault.id) },
                tagsEncrypted = vault.tagsEncrypted,
                deletedItems = vault.deletedItems?.map { deletedItemsMapper.mapToDomain(json = it, vault.id) },
                deletedItemsEncrypted = vault.deletedItemsEncrypted,
                encryption = encryption?.let { mapToDomain(it) },
            )
        }
    }

    private fun mapToJson(domain: EncryptionSpec): EncryptionSpecJson {
        return with(domain) {
            EncryptionSpecJson(
                seedHash = seedHash,
                reference = reference,
                kdfSpec = kdfSpec.asEntity(),
            )
        }
    }

    private fun mapToDomain(json: EncryptionSpecJson): EncryptionSpec {
        return with(json) {
            EncryptionSpec(
                seedHash = seedHash,
                reference = reference,
                kdfSpec = kdfSpec.asDomain(),
            )
        }
    }
}