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
import com.twofasapp.data.main.domain.InvalidSchemaVersion
import com.twofasapp.data.main.domain.VaultBackup
import com.twofasapp.data.main.domain.VaultKeys
import com.twofasapp.data.main.mapper.DeletedItemsMapper
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import com.twofasapp.data.main.mapper.LoginMapper
import com.twofasapp.data.main.mapper.TagMapper
import com.twofasapp.data.main.mapper.VaultBackupMapper
import com.twofasapp.data.main.mapper.VaultDataForBrowserMapper
import com.twofasapp.data.main.remote.model.BrowserExtensionVaultDataCompressedJson
import com.twofasapp.data.main.remote.model.DeletedItemJson
import com.twofasapp.data.main.remote.model.LoginJson
import com.twofasapp.data.main.remote.model.TagJson
import com.twofasapp.data.main.remote.model.VaultBackupJsonV1
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
    private val loginMapper: LoginMapper,
    private val tagMapper: TagMapper,
    private val itemEncryptionMapper: ItemEncryptionMapper,
    private val vaultBackupMapper: VaultBackupMapper,
    private val deletedItemsMapper: DeletedItemsMapper,
    private val vaultDataForBrowserMapper: VaultDataForBrowserMapper,
    private val vaultsRepository: VaultsRepository,
    private val loginsRepository: LoginsRepository,
    private val vaultCryptoScope: VaultCryptoScope,
    private val vaultKeysRepository: VaultKeysRepository,
    private val securityRepository: SecurityRepository,
    private val deletedItemsRepository: DeletedItemsRepository,
    private val tagsRepository: TagsRepository,
) : BackupRepository {

    override suspend fun createVaultBackup(vaultId: String, includeDeleted: Boolean): VaultBackup {
        return withContext(dispatchers.io) {
            val vault = vaultsRepository.getVault(vaultId)
            val logins = vaultCryptoScope.withVaultCipher(vault) {
                loginsRepository.getLogins().mapNotNull { login ->
                    itemEncryptionMapper.decryptLogin(
                        itemEncrypted = login,
                        vaultCipher = this,
                        decryptPassword = true,
                    )
                }
            }

            val tags = tagsRepository.getTags(vaultId)

            VaultBackup(
                schemaVersion = VaultBackup.CurrentSchema,
                originOs = appBuild.os,
                originAppVersionCode = appBuild.versionCode,
                originAppVersionName = appBuild.versionName,
                originDeviceId = device.uniqueId(),
                originDeviceName = device.name(),
                vaultId = vault.id,
                vaultName = vault.name,
                vaultCreatedAt = vault.createdAt,
                vaultUpdatedAt = vault.updatedAt,
                logins = logins.filter { it.deleted.not() },
                loginsEncrypted = null,
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
                    logins = null,
                    loginsEncrypted = vaultBackup.logins
                        ?.map { login ->
                            encryptWithExternalKey(
                                json.encodeToString(loginMapper.mapToJson(login)),
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
        val jsonElement = json.parseToJsonElement(content)
        val schemaVersion = jsonElement.jsonObject["schemaVersion"]!!.jsonPrimitive.int

        return withContext(dispatchers.io) {
            try {
                val serializer = when (schemaVersion) {
                    1 -> VaultBackupJsonV1.serializer()
                    else -> VaultBackupJsonV1.serializer()
                }

                vaultBackupMapper.mapToDomain(
                    json = json.decodeFromString(serializer, content),
                    deviceIdFallback = device.uniqueId(),
                )
            } catch (e: Exception) {
                if (VaultBackup.CurrentSchema != schemaVersion) {
                    throw InvalidSchemaVersion("Your current app version supports backups up to version ${VaultBackup.CurrentSchema}. The file you're trying to read is version $schemaVersion. Please update your application to latest version.")
                } else {
                    throw e
                }
            }
        }
    }

    override suspend fun decryptVaultBackup(vaultBackup: VaultBackup, vaultKeys: VaultKeys): VaultBackup {
        return withContext(dispatchers.io) {
            vaultCryptoScope.withVaultCipher(vaultKeys) {
                val logins = vaultBackup.loginsEncrypted.orEmpty().map { encryptedLoginJson ->
                    json.decodeFromString<LoginJson>(
                        decryptWithExternalKey(EncryptedBytes(encryptedLoginJson.decodeBase64())),
                    ).let { loginMapper.mapToDomain(json = it, vaultBackup.vaultId) }
                }

                val tags = vaultBackup.tagsEncrypted.orEmpty().map { encryptedTagJson ->
                    json.decodeFromString<TagJson>(
                        decryptWithExternalKey(EncryptedBytes(encryptedTagJson.decodeBase64())),
                    ).let { tagMapper.mapToDomain(json = it, vaultBackup.vaultId) }
                }

                val deletedItems = vaultBackup.deletedItemsEncrypted.orEmpty().map { encryptedLoginJson ->
                    json.decodeFromString<DeletedItemJson>(
                        decryptWithExternalKey(EncryptedBytes(encryptedLoginJson.decodeBase64())),
                    ).let { deletedItemsMapper.mapToDomain(json = it, vaultId = vaultBackup.vaultId) }
                }

                vaultBackup.copy(
                    logins = logins,
                    tags = tags,
                    deletedItems = deletedItems,
                    loginsEncrypted = null,
                    tagsEncrypted = null,
                    deletedItemsEncrypted = null,
                    encryption = null,
                )
            }
        }
    }

    override suspend fun decryptVaultBackup(vaultBackup: VaultBackup, password: String, seed: Seed): VaultBackup {
        return withContext(dispatchers.io) {
            if (vaultBackup.encryption == null) {
                vaultBackup
            } else {
                val masterKey = securityRepository.generateMasterKey(
                    password = password,
                    seed = seed,
                    kdfSpec = vaultBackup.encryption.kdfSpec,
                )
                val vaultKeys = vaultKeysRepository.generateVaultKeys(masterKeyHex = masterKey.hashHex, vaultId = vaultBackup.vaultId)

                decryptVaultBackup(vaultBackup, vaultKeys)
            }
        }
    }

    override suspend fun decryptVaultBackup(vaultBackup: VaultBackup, masterKey: ByteArray, seed: Seed): VaultBackup {
        return withContext(dispatchers.io) {
            if (vaultBackup.encryption == null) {
                vaultBackup
            } else {
                val vaultKeys = vaultKeysRepository.generateVaultKeys(masterKeyHex = masterKey.encodeHex(), vaultId = vaultBackup.vaultId)

                decryptVaultBackup(vaultBackup, vaultKeys)
            }
        }
    }

    override suspend fun createCompressedVaultDataForBrowserExtension(
        vaultId: String,
        deviceId: String,
        encryptionPassKey: ByteArray,
    ): String {
        return withContext(dispatchers.io) {
            val vaultData = createVaultBackup(vaultId = vaultId, includeDeleted = false)

            val vaultDataJson = vaultDataForBrowserMapper.mapToJson(
                vaultBackup = vaultData,
                deviceId = deviceId,
                encryptionKey = encryptionPassKey,
            )

            val vaultDataCompressedJson = BrowserExtensionVaultDataCompressedJson(
                logins = Gzip.compress(json.encodeToString(vaultDataJson.logins)).encodeBase64(),
                tags = Gzip.compress(json.encodeToString(vaultDataJson.tags)).encodeBase64(),
            )

            json.encodeToString(vaultDataCompressedJson)
        }
    }
}