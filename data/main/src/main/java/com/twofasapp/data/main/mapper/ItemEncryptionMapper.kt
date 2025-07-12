/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.ItemEncrypted
import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.data.main.VaultCipher
import com.twofasapp.data.main.domain.VaultKeysExpiredException
import com.twofasapp.data.main.local.model.items.LoginContentEntityV1
import kotlinx.serialization.json.Json

class ItemEncryptionMapper(
    private val json: Json,
    private val iconTypeMapper: LoginIconTypeMapper,
    private val uriMapper: LoginUriMapper,
) {
    fun decryptLogin(
        itemEncrypted: ItemEncrypted,
        vaultCipher: VaultCipher,
        decryptPassword: Boolean = false,
    ): Login? {
        return try {
            val contentJson = when (itemEncrypted.securityType) {
                SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(itemEncrypted.content)
                SecurityType.Tier2 -> vaultCipher.decryptWithTrustedKey(itemEncrypted.content)
                SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(itemEncrypted.content)
            }

            val serializer = when (itemEncrypted.contentType) {
                "login" -> LoginContentEntityV1.serializer()
                else -> return null
            }

            val content = json.decodeFromString(serializer, contentJson)

            return Login(
                id = itemEncrypted.id,
                vaultId = itemEncrypted.vaultId,
                createdAt = itemEncrypted.createdAt,
                updatedAt = itemEncrypted.updatedAt,
                deletedAt = itemEncrypted.deletedAt,
                deleted = itemEncrypted.deleted,
                securityType = itemEncrypted.securityType,
                tagIds = itemEncrypted.tagIds,
                name = content.name,
                username = content.username,
                password = content.password?.let {
                    if (decryptPassword) {
                        SecretField.Visible(
                            when (itemEncrypted.securityType) {
                                SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(it)
                                SecurityType.Tier2 -> vaultCipher.decryptWithSecretKey(it)
                                SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(it)
                            },
                        )
                    } else {
                        SecretField.Hidden(it)
                    }
                },
                uris = content.uris.map { uriMapper.mapToDomain(it) },
                iconType = iconTypeMapper.mapToDomainFromEntity(content.iconType),
                iconUriIndex = content.iconUriIndex,
                customImageUrl = content.customImageUrl,
                labelText = content.labelText,
                labelColor = content.labelColor,
                notes = content.notes,
            )
        } catch (e: VaultKeysExpiredException) {
            null
        }
    }

    fun encryptLogins(
        logins: List<Login>,
        vaultCipher: VaultCipher,
    ): List<ItemEncrypted> {
        return logins.map { encryptLogin(login = it, vaultCipher = vaultCipher) }
    }

    fun encryptLogin(
        login: Login,
        vaultCipher: VaultCipher,
    ): ItemEncrypted {
        val contentLoginEntity = LoginContentEntityV1(
            name = login.name,
            username = login.username,
            password = when (login.password) {
                is SecretField.Hidden -> (login.password as SecretField.Hidden).value
                is SecretField.Visible -> {
                    if ((login.password as SecretField.Visible).value.isBlank()) {
                        null
                    } else {
                        when (login.securityType) {
                            SecurityType.Tier1 -> vaultCipher.encryptWithSecretKey((login.password as SecretField.Visible).value)
                            SecurityType.Tier2 -> vaultCipher.encryptWithSecretKey((login.password as SecretField.Visible).value)
                            SecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey((login.password as SecretField.Visible).value)
                        }
                    }
                }

                null -> null
            },
            uris = login.uris.map { uriMapper.mapToEntity(it) },
            iconType = iconTypeMapper.mapToEntity(login.iconType),
            iconUriIndex = login.iconUriIndex,
            customImageUrl = login.customImageUrl,
            labelText = login.labelText,
            labelColor = login.labelColor,
            notes = login.notes,
        )

        val contentJson = json.encodeToString(contentLoginEntity)

        val contentEncrypted = when (login.securityType) {
            SecurityType.Tier1 -> vaultCipher.encryptWithSecretKey(contentJson)
            SecurityType.Tier2 -> vaultCipher.encryptWithTrustedKey(contentJson)
            SecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey(contentJson)
        }

        return ItemEncrypted(
            id = login.id,
            vaultId = login.vaultId,
            createdAt = login.createdAt,
            updatedAt = login.updatedAt,
            deletedAt = login.deletedAt,
            deleted = login.deleted,
            securityType = login.securityType,
            tagIds = login.tagIds,
            contentType = "login",
            contentVersion = 1,
            content = contentEncrypted,
        )
    }

    fun decryptPassword(
        login: Login,
        vaultCipher: VaultCipher,
    ): String? {
        return try {
            val password = when (login.password) {
                is SecretField.Hidden -> (login.password as SecretField.Hidden).value
                is SecretField.Visible -> return (login.password as SecretField.Visible).value
                null -> null
            }

            password?.let {
                when (login.securityType) {
                    SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(it)
                    SecurityType.Tier2 -> vaultCipher.decryptWithSecretKey(it)
                    SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(it)
                }
            }
        } catch (e: VaultKeysExpiredException) {
            null
        }
    }

    fun withVisiblePassword(
        login: Login?,
        vaultCipher: VaultCipher,
    ): Login? {
        return try {
            login?.copy(
                password = when (login.password) {
                    is SecretField.Hidden -> {
                        SecretField.Visible(
                            when (login.securityType) {
                                SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey((login.password as SecretField.Hidden).value)
                                SecurityType.Tier2 -> vaultCipher.decryptWithSecretKey((login.password as SecretField.Hidden).value)
                                SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey((login.password as SecretField.Hidden).value)
                            },
                        )
                    }

                    is SecretField.Visible -> login.password
                    null -> null
                },
            )
        } catch (e: VaultKeysExpiredException) {
            null
        }
    }

    fun withHiddenPassword(
        login: Login?,
        vaultCipher: VaultCipher,
    ): Login? {
        return try {
            login?.copy(
                password = when (login.password) {
                    is SecretField.Hidden -> login.password
                    is SecretField.Visible -> {
                        SecretField.Hidden(
                            when (login.securityType) {
                                SecurityType.Tier1 -> vaultCipher.encryptWithSecretKey((login.password as SecretField.Visible).value)
                                SecurityType.Tier2 -> vaultCipher.encryptWithSecretKey((login.password as SecretField.Visible).value)
                                SecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey((login.password as SecretField.Visible).value)
                            },
                        )
                    }

                    null -> null
                },
            )
        } catch (e: VaultKeysExpiredException) {
            null
        }
    }
}