/*
* SPDX-License-Identifier: BUSL-1.1
*
* Copyright © 2025 Two Factor Authentication Service, Inc.
* Licensed under the Business Source License 1.1
* See LICENSE file for full terms
*/

package com.twofasapp.data.main

import android.content.Context
import android.net.Uri
import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.core.common.build.Device
import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.crypto.Gzip
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.domain.crypto.EncryptionSpec
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.core.common.ktx.encodeBase64
import com.twofasapp.core.common.ktx.encodeHex
import com.twofasapp.core.common.ktx.readTextFile
import com.twofasapp.data.main.domain.InvalidSchemaVersionException
import com.twofasapp.data.main.domain.VaultBackup
import com.twofasapp.data.main.domain.VaultKeys
import com.twofasapp.data.main.mapper.DeletedItemsMapper
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import com.twofasapp.data.main.mapper.ItemMapper
import com.twofasapp.data.main.mapper.TagMapper
import com.twofasapp.data.main.mapper.VaultBackupMapper
import com.twofasapp.data.main.mapper.VaultDataForBrowserMapper
import com.twofasapp.data.main.remote.model.BrowserExtensionVaultDataCompressedJson
import com.twofasapp.data.main.remote.model.DeletedItemJson
import com.twofasapp.data.main.remote.model.ItemJson
import com.twofasapp.data.main.remote.model.TagJson
import com.twofasapp.data.main.remote.model.vaultbackup.LoginJson
import com.twofasapp.data.security.crypto.Seed
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class BackupRepositoryImpl(
    private val context: Context,
    private val dispatchers: Dispatchers,
    private val appBuild: AppBuild,
    private val device: Device,
    private val json: Json,
    private val itemMapper: ItemMapper,
    private val tagMapper: TagMapper,
    private val itemEncryptionMapper: ItemEncryptionMapper,
    private val vaultBackupMapper: VaultBackupMapper,
    private val deletedItemsMapper: DeletedItemsMapper,
    private val vaultDataForBrowserMapper: VaultDataForBrowserMapper,
    private val vaultsRepository: VaultsRepository,
    private val itemsRepository: ItemsRepository,
    private val vaultCryptoScope: VaultCryptoScope,
    private val vaultKeysRepository: VaultKeysRepository,
    private val securityRepository: SecurityRepository,
    private val deletedItemsRepository: DeletedItemsRepository,
    private val tagsRepository: TagsRepository,
) : BackupRepository {

    override suspend fun createVaultBackup(vaultId: String, includeDeleted: Boolean, decryptSecretFields: Boolean): VaultBackup {
        return withContext(dispatchers.io) {
            val vault = vaultsRepository.getVault(vaultId)
            val items = vaultCryptoScope.withVaultCipher(vault) {
                itemsRepository.getItems().mapNotNull { item ->
                    itemEncryptionMapper.decryptItem(
                        itemEncrypted = item,
                        vaultCipher = this,
                        decryptSecretFields = decryptSecretFields,
                    )
                }
            }

            val tags = tagsRepository.getTags(vaultId)

            VaultBackup(
                schemaVersion = VaultBackup.CurrentSchema,
                originOs = appBuild.os,
                originAppVersionCode = appBuild.versionCode.toInt(),
                originAppVersionName = appBuild.versionName,
                originDeviceId = device.uniqueId(),
                originDeviceName = device.name(),
                vaultId = vault.id,
                vaultName = vault.name,
                vaultCreatedAt = vault.createdAt,
                vaultUpdatedAt = vault.updatedAt,
                items = items.filter { it.deleted.not() },
                itemsEncrypted = null,
                tags = tags,
                tagsEncrypted = null,
                deletedItems = if (includeDeleted) {
                    deletedItemsRepository.getDeletedItems(vault.id)
                } else {
                    null
                },
                deletedItemsEncrypted = null,
                encryption = null,
            )
        }
    }

    override suspend fun encryptVaultBackup(vaultBackup: VaultBackup): VaultBackup {
        return withContext(dispatchers.io) {
            val vaultKeys = vaultKeysRepository.getVaultKeys(vaultBackup.vaultId)
            val vaultHashes = vaultKeysRepository.generateVaultHashes(
                seedHex = securityRepository.getSeed().seedHex,
                vaultId = vaultBackup.vaultId,
            )

            vaultCryptoScope.withVaultCipher(vaultKeys) {
                vaultBackup.copy(
                    items = null,
                    itemsEncrypted = vaultBackup.items
                        ?.map { item ->
                            encryptWithExternalKey(
                                json.encodeToString(itemMapper.mapToJson(item)),
                            ).encodeBase64()
                        },
                    tags = null,
                    tagsEncrypted = vaultBackup.tags
                        ?.map { tag ->
                            encryptWithExternalKey(
                                json.encodeToString(tagMapper.mapToJson(tag)),
                            ).encodeBase64()
                        },
                    deletedItems = null,
                    deletedItemsEncrypted = vaultBackup.deletedItems
                        ?.map { item ->
                            encryptWithExternalKey(
                                json.encodeToString(item.let(deletedItemsMapper::mapToJson)),
                            ).encodeBase64()
                        },
                    encryption = EncryptionSpec(
                        seedHash = vaultHashes.seedHashBase64,
                        reference = encryptWithExternalKey(vaultBackup.vaultId).encodeBase64(),
                        kdfSpec = securityRepository.getMasterKeyKdfSpec(),
                    ),
                )
            }
        }
    }

    override suspend fun serializeVaultBackup(vaultBackup: VaultBackup): String {
        return json.encodeToString(vaultBackupMapper.mapToJson(vaultBackup))
    }

    override suspend fun readVaultBackup(fileUri: Uri): VaultBackup {
        return withContext(dispatchers.io) {
            readVaultBackup(context.readTextFile(fileUri = fileUri, limitFileSize = false))
        }
    }

    override suspend fun readVaultBackup(content: String): VaultBackup {
        return withContext(dispatchers.io) {
            val jsonElement = json.parseToJsonElement(content)
            val schemaVersion = jsonElement.jsonObject["schemaVersion"]!!.jsonPrimitive.int

            if (schemaVersion > VaultBackup.CurrentSchema) {
                throw InvalidSchemaVersionException(
                    msg = "Cloud sync failed. The Vault you’re trying to synchronize was created in a newer version $schemaVersion, which is not supported in your current version. Please update your app to synchronize it.",
                    backupSchemaVersion = schemaVersion,
                )
            }

            vaultBackupMapper.mapToDomain(
                schemaVersion = schemaVersion,
                content = content,
                deviceIdFallback = device.uniqueId(),
            )
        }
    }

    override suspend fun decryptVaultBackup(vaultBackup: VaultBackup, vaultKeys: VaultKeys, decryptSecretFields: Boolean): VaultBackup {
        return withContext(dispatchers.io) {
            vaultCryptoScope.withVaultCipher(vaultKeys) {
                val items = vaultBackup.itemsEncrypted.orEmpty().map { encryptedItemJson ->
                    val decryptedItemJson = decryptWithExternalKey(EncryptedBytes(encryptedItemJson.decodeBase64()))

                    when (vaultBackup.schemaVersion) {
                        1 -> {
                            json.decodeFromString<LoginJson>(decryptedItemJson)
                                .let { itemMapper.mapToDomainFromV1(json = it, vaultId = vaultBackup.vaultId) }
                        }

                        2 -> {
                            json
                                // Read Item from string
                                .decodeFromString<ItemJson>(decryptedItemJson)
                                .let {
                                    // Item is decrypted but its content still have secret fields encrypted (SecretField.Encrypted)
                                    itemMapper.mapToDomain(
                                        json = it,
                                        vaultId = vaultBackup.vaultId,
                                        tagIds = it.tags,
                                        hasSecretFieldsEncrypted = true,
                                    )
                                }
                                .let { item ->
                                    // We need to now decrypt the content of the item
                                    if (decryptSecretFields) {
                                        item.copy(
                                            content = itemEncryptionMapper.decryptSecretFields(
                                                vaultCipher = this,
                                                securityType = item.securityType,
                                                content = item.content,
                                            ),
                                        )
                                    } else {
                                        item
                                    }
                                }
                        }

                        else -> {
                            throw IllegalArgumentException("Unsupported schema version: ${vaultBackup.schemaVersion}")
                        }
                    }
                }

                val tags = vaultBackup.tagsEncrypted.orEmpty().map { encryptedTagJson ->
                    json.decodeFromString<TagJson>(
                        decryptWithExternalKey(EncryptedBytes(encryptedTagJson.decodeBase64())),
                    ).let { tagMapper.mapToDomain(json = it, vaultBackup.vaultId) }
                }

                val deletedItems = vaultBackup.deletedItemsEncrypted.orEmpty().map { encryptedDeletedItemJson ->
                    json.decodeFromString<DeletedItemJson>(
                        decryptWithExternalKey(EncryptedBytes(encryptedDeletedItemJson.decodeBase64())),
                    ).let { deletedItemsMapper.mapToDomain(json = it, vaultId = vaultBackup.vaultId) }
                }

                vaultBackup.copy(
                    items = items,
                    tags = tags,
                    deletedItems = deletedItems,
                    itemsEncrypted = null,
                    tagsEncrypted = null,
                    deletedItemsEncrypted = null,
                    encryption = null,
                )
            }
        }
    }

    override suspend fun decryptVaultBackup(vaultBackup: VaultBackup, masterKey: ByteArray, seed: Seed, decryptSecretFields: Boolean): VaultBackup {
        return withContext(dispatchers.io) {
            if (vaultBackup.encryption == null) {
                vaultBackup
            } else {
                val vaultKeys = vaultKeysRepository.generateVaultKeys(masterKeyHex = masterKey.encodeHex(), vaultId = vaultBackup.vaultId)

                decryptVaultBackup(vaultBackup, vaultKeys, decryptSecretFields)
            }
        }
    }

    override suspend fun createCompressedVaultDataForBrowserExtension(
        version: Int,
        vaultId: String,
        deviceId: String,
        encryptionKey: ByteArray,
    ): String {
        return withContext(dispatchers.io) {
            val vaultData = createVaultBackup(vaultId = vaultId, includeDeleted = false, decryptSecretFields = true) // TODO: BEv2

            val vaultDataJson = when (version) {
                1 -> {
                    vaultDataForBrowserMapper.mapToJsonV1(
                        vaultBackup = vaultData,
                        deviceId = deviceId,
                        encryptionKey = encryptionKey,
                    )
                }

                else -> {
                    vaultDataForBrowserMapper.mapToJson(
                        vaultBackup = vaultData,
                        deviceId = deviceId,
                        encryptionKey = encryptionKey,
                    )
                }
            }

            val vaultDataCompressedJson = BrowserExtensionVaultDataCompressedJson(
                logins = vaultDataJson.logins?.let { logins -> Gzip.compress(json.encodeToString(logins)).encodeBase64() },
                items = vaultDataJson.items?.let { items -> Gzip.compress(json.encodeToString(items)).encodeBase64() },
                tags = Gzip.compress(json.encodeToString(vaultDataJson.tags)).encodeBase64(),
            )

            json.encodeToString(vaultDataCompressedJson)
        }
    }
}