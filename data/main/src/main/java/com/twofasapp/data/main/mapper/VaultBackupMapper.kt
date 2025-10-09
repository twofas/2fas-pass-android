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
import com.twofasapp.data.main.remote.model.vaultbackup.VaultBackupJsonV1
import com.twofasapp.data.main.remote.model.vaultbackup.VaultBackupJsonV2
import kotlinx.serialization.json.Json

internal class VaultBackupMapper(
    private val jsonSerializer: Json,
    private val itemMapper: ItemMapper,
    private val tagMapper: TagMapper,
    private val deletedItemsMapper: DeletedItemsMapper,
) {
    fun mapToJson(domain: VaultBackup): VaultBackupJsonV2 {
        return with(domain) {
            VaultBackupJsonV2(
                schemaVersion = VaultBackup.CurrentSchema,
                origin = VaultBackupJsonV2.Origin(
                    os = originOs,
                    appVersionCode = originAppVersionCode,
                    appVersionName = originAppVersionName,
                    deviceId = originDeviceId,
                    deviceName = originDeviceName,
                ),
                encryption = encryption?.let { mapToJson(it) },
                vault = VaultBackupJsonV2.Vault(
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
            1 -> mapToDomainFromV1(json = jsonSerializer.decodeFromString(VaultBackupJsonV1.serializer(), content), deviceIdFallback = deviceIdFallback)
            2 -> mapToDomainFromV2(json = jsonSerializer.decodeFromString(VaultBackupJsonV2.serializer(), content), deviceIdFallback = deviceIdFallback)
            else -> throw IllegalArgumentException("Unsupported schema version: $schemaVersion")
        }
    }

    private fun mapToDomainFromV1(json: VaultBackupJsonV1, deviceIdFallback: String): VaultBackup {
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

    private fun mapToDomainFromV2(json: VaultBackupJsonV2, deviceIdFallback: String): VaultBackup {
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