/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.clearText
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemEncrypted
import com.twofasapp.data.main.VaultCipher
import com.twofasapp.data.main.domain.VaultKeysExpiredException
import com.twofasapp.data.main.local.model.items.LoginContentEntityV1
import com.twofasapp.data.main.local.model.items.SecureNoteContentEntityV1
import kotlinx.serialization.json.Json

class ItemEncryptionMapper(
    private val json: Json,
    private val iconTypeMapper: IconTypeMapper,
    private val uriMapper: ItemUriMapper,
) {
    fun decryptItem(
        itemEncrypted: ItemEncrypted,
        vaultCipher: VaultCipher,
        decryptSecretFields: Boolean = false,
    ): Item? {
        return try {
            val contentEntityJson = when (itemEncrypted.securityType) {
                SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(itemEncrypted.content)
                SecurityType.Tier2 -> vaultCipher.decryptWithTrustedKey(itemEncrypted.content)
                SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(itemEncrypted.content)
            }

            val serializer = when (itemEncrypted.contentType) {
                "login" -> LoginContentEntityV1.serializer()
                "secureNote" -> SecureNoteContentEntityV1.serializer()
                else -> return itemEncrypted.asDecrypted(content = ItemContent.Unknown(rawJson = contentEntityJson)) // TODO: Decrypt unknown item
            }

            val contentEntity = json.decodeFromString(serializer, contentEntityJson)

            val content = when (contentEntity) {
                is LoginContentEntityV1 -> {
                    ItemContent.Login(
                        name = contentEntity.name,
                        username = contentEntity.username,
                        password = contentEntity.password?.let {
                            if (decryptSecretFields) {
                                SecretField.ClearText(
                                    when (itemEncrypted.securityType) {
                                        SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(it)
                                        SecurityType.Tier2 -> vaultCipher.decryptWithSecretKey(it)
                                        SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(it)
                                    },
                                )
                            } else {
                                SecretField.Encrypted(it)
                            }
                        },
                        uris = contentEntity.uris.map { uriMapper.mapToDomain(it) },
                        iconType = iconTypeMapper.mapToDomainFromEntity(contentEntity.iconType),
                        iconUriIndex = contentEntity.iconUriIndex,
                        customImageUrl = contentEntity.customImageUrl,
                        labelText = contentEntity.labelText,
                        labelColor = contentEntity.labelColor,
                        notes = contentEntity.notes,
                    )
                }

                is SecureNoteContentEntityV1 -> {
                    ItemContent.SecureNote(
                        name = contentEntity.name,
                        text = contentEntity.text?.let {
                            when (itemEncrypted.securityType) {
                                SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(it)
                                SecurityType.Tier2 -> vaultCipher.decryptWithSecretKey(it)
                                SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(it)
                            }
                        },
                    )
                }
            }

            itemEncrypted.asDecrypted(content = content)
        } catch (_: VaultKeysExpiredException) {
            null
        }
    }

    fun encryptItem(
        item: Item,
        vaultCipher: VaultCipher,
    ): ItemEncrypted {
        val contentEntityJson = item.content.let { content ->
            when (content) {
                is ItemContent.Unknown -> {
                    // TODO: Encrypt unknown item
                    content.rawJson
                }

                is ItemContent.Login -> {
                    json.encodeToString(
                        LoginContentEntityV1(
                            name = content.name,
                            username = content.username,
                            password = when (content.password) {
                                is SecretField.Encrypted -> (content.password as SecretField.Encrypted).value
                                is SecretField.ClearText -> {
                                    if (content.password.clearText.isBlank()) {
                                        null
                                    } else {
                                        when (item.securityType) {
                                            SecurityType.Tier1 -> vaultCipher.encryptWithSecretKey(content.password.clearText)
                                            SecurityType.Tier2 -> vaultCipher.encryptWithSecretKey(content.password.clearText)
                                            SecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey(content.password.clearText)
                                        }
                                    }
                                }

                                null -> null
                            },
                            uris = content.uris.map { uriMapper.mapToEntity(it) },
                            iconType = iconTypeMapper.mapToEntity(content.iconType),
                            iconUriIndex = content.iconUriIndex,
                            customImageUrl = content.customImageUrl,
                            labelText = content.labelText,
                            labelColor = content.labelColor,
                            notes = content.notes,
                        ),
                    )
                }

                is ItemContent.SecureNote -> {
                    json.encodeToString(
                        SecureNoteContentEntityV1(
                            name = content.name,
                            text = content.text?.let {
                                when (item.securityType) {
                                    SecurityType.Tier1 -> vaultCipher.encryptWithSecretKey(it)
                                    SecurityType.Tier2 -> vaultCipher.encryptWithSecretKey(it)
                                    SecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey(it)
                                }
                            },
                        ),
                    )
                }
            }
        }

        val contentEntityJsonEncrypted = when (item.securityType) {
            SecurityType.Tier1 -> vaultCipher.encryptWithSecretKey(contentEntityJson)
            SecurityType.Tier2 -> vaultCipher.encryptWithTrustedKey(contentEntityJson)
            SecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey(contentEntityJson)
        }

        return item.asEncrypted(content = contentEntityJsonEncrypted)
    }

    fun encryptItems(
        items: List<Item>,
        vaultCipher: VaultCipher,
    ): List<ItemEncrypted> {
        return items.map { encryptItem(item = it, vaultCipher = vaultCipher) }
    }

    fun decryptSecretField(
        secretField: SecretField?,
        securityType: SecurityType,
        vaultCipher: VaultCipher,
    ): String? {
        return try {
            if (secretField == null) {
                return null
            }

            when (secretField) {
                is SecretField.Encrypted -> {
                    when (securityType) {
                        SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(secretField.value)
                        SecurityType.Tier2 -> vaultCipher.decryptWithSecretKey(secretField.value)
                        SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(secretField.value)
                    }
                }

                is SecretField.ClearText -> {
                    secretField.value
                }
            }
        } catch (_: VaultKeysExpiredException) {
            null
        }
    }

    private fun Item.asEncrypted(content: EncryptedBytes): ItemEncrypted {
        return ItemEncrypted(
            id = id,
            vaultId = vaultId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt,
            deleted = deleted,
            securityType = securityType,
            contentType = contentType,
            contentVersion = contentVersion,
            content = content,
            tagIds = tagIds,
        )
    }

    private fun ItemEncrypted.asDecrypted(content: ItemContent): Item {
        return Item(
            id = id,
            vaultId = vaultId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt,
            deleted = deleted,
            securityType = securityType,
            contentType = contentType,
            contentVersion = contentVersion,
            content = content,
            tagIds = tagIds,
        )
    }
}