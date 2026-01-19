/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.crypto.encrypt
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.clearText
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.common.domain.items.ItemEncrypted
import com.twofasapp.data.main.VaultCipher
import com.twofasapp.data.main.domain.VaultKeysExpiredException
import com.twofasapp.data.main.local.model.items.LoginContentEntityV1
import com.twofasapp.data.main.local.model.items.PaymentCardContentEntityV1
import com.twofasapp.data.main.local.model.items.SecureNoteContentEntityV1
import kotlinx.serialization.json.Json

class ItemEncryptionMapper(
    private val json: Json,
    private val iconTypeMapper: IconTypeMapper,
    private val uriMapper: ItemUriMapper,
    private val unknownItemEncryptionMapper: UnknownItemEncryptionMapper,
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

            val content = when (itemEncrypted.contentType) {
                is ItemContentType.Login -> {
                    val contentEntity = json.decodeFromString(LoginContentEntityV1.serializer(), contentEntityJson)

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

                is ItemContentType.SecureNote -> {
                    val contentEntity = json.decodeFromString(SecureNoteContentEntityV1.serializer(), contentEntityJson)

                    ItemContent.SecureNote(
                        name = contentEntity.name,
                        text = contentEntity.text?.let {
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
                        additionalInfo = contentEntity.additionalInfo,
                    )
                }

                is ItemContentType.PaymentCard -> {
                    val contentEntity = json.decodeFromString(PaymentCardContentEntityV1.serializer(), contentEntityJson)

                    ItemContent.PaymentCard(
                        name = contentEntity.name,
                        cardHolder = contentEntity.cardHolder,
                        cardNumber = contentEntity.cardNumber?.let {
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
                        expirationDate = contentEntity.expirationDate?.let {
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
                        securityCode = contentEntity.securityCode?.let {
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
                        cardNumberMask = contentEntity.cardNumberMask,
                        cardIssuer = ItemContent.PaymentCard.Issuer.fromCode(contentEntity.cardIssuer),
                        notes = contentEntity.notes,
                    )
                }

                is ItemContentType.Unknown -> unknownItemEncryptionMapper.decrypt(
                    rawJson = contentEntityJson,
                    securityType = itemEncrypted.securityType,
                    vaultCipher = vaultCipher,
                    decryptSecretFields = decryptSecretFields,
                )
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
                is ItemContent.Unknown -> unknownItemEncryptionMapper.encrypt(
                    rawJson = content.rawJson,
                    securityType = item.securityType,
                    vaultCipher = vaultCipher,
                )

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
                            text = when (content.text) {
                                is SecretField.Encrypted -> (content.text as SecretField.Encrypted).value
                                is SecretField.ClearText -> {
                                    if (content.text.clearText.isBlank()) {
                                        null
                                    } else {
                                        when (item.securityType) {
                                            SecurityType.Tier1 -> vaultCipher.encryptWithSecretKey(content.text.clearText)
                                            SecurityType.Tier2 -> vaultCipher.encryptWithSecretKey(content.text.clearText)
                                            SecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey(content.text.clearText)
                                        }
                                    }
                                }

                                null -> null
                            },
                            additionalInfo = content.additionalInfo,
                        ),
                    )
                }

                is ItemContent.PaymentCard -> {
                    json.encodeToString(
                        PaymentCardContentEntityV1(
                            name = content.name,
                            cardHolder = content.cardHolder,
                            cardNumber = when (content.cardNumber) {
                                is SecretField.Encrypted -> (content.cardNumber as SecretField.Encrypted).value
                                is SecretField.ClearText -> {
                                    if (content.cardNumber.clearText.isBlank()) {
                                        null
                                    } else {
                                        when (item.securityType) {
                                            SecurityType.Tier1 -> vaultCipher.encryptWithSecretKey(content.cardNumber.clearText)
                                            SecurityType.Tier2 -> vaultCipher.encryptWithSecretKey(content.cardNumber.clearText)
                                            SecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey(content.cardNumber.clearText)
                                        }
                                    }
                                }

                                null -> null
                            },
                            expirationDate = when (content.expirationDate) {
                                is SecretField.Encrypted -> (content.expirationDate as SecretField.Encrypted).value
                                is SecretField.ClearText -> {
                                    if (content.expirationDate.clearText.isBlank()) {
                                        null
                                    } else {
                                        when (item.securityType) {
                                            SecurityType.Tier1 -> vaultCipher.encryptWithSecretKey(content.expirationDate.clearText)
                                            SecurityType.Tier2 -> vaultCipher.encryptWithSecretKey(content.expirationDate.clearText)
                                            SecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey(content.expirationDate.clearText)
                                        }
                                    }
                                }

                                null -> null
                            },
                            securityCode = when (content.securityCode) {
                                is SecretField.Encrypted -> (content.securityCode as SecretField.Encrypted).value
                                is SecretField.ClearText -> {
                                    if (content.securityCode.clearText.isBlank()) {
                                        null
                                    } else {
                                        when (item.securityType) {
                                            SecurityType.Tier1 -> vaultCipher.encryptWithSecretKey(content.securityCode.clearText)
                                            SecurityType.Tier2 -> vaultCipher.encryptWithSecretKey(content.securityCode.clearText)
                                            SecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey(content.securityCode.clearText)
                                        }
                                    }
                                }

                                null -> null
                            },
                            cardNumberMask = content.cardNumberMask,
                            cardIssuer = content.cardIssuer?.code,
                            notes = content.notes,
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
            content = content,
            tagIds = tagIds,
        )
    }

    fun decryptSecretFields(
        vaultCipher: VaultCipher,
        securityType: SecurityType,
        content: ItemContent,
    ): ItemContent {
        return when (content) {
            is ItemContent.Login -> {
                content.copy(
                    password = content.password?.let {
                        when (it) {
                            is SecretField.ClearText -> it
                            is SecretField.Encrypted -> {
                                SecretField.ClearText(
                                    when (securityType) {
                                        SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(it.value)
                                        SecurityType.Tier2 -> vaultCipher.decryptWithSecretKey(it.value)
                                        SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(it.value)
                                    },
                                )
                            }
                        }
                    },
                )
            }

            is ItemContent.SecureNote -> {
                content.copy(
                    text = content.text?.let {
                        when (it) {
                            is SecretField.ClearText -> it
                            is SecretField.Encrypted -> {
                                SecretField.ClearText(
                                    when (securityType) {
                                        SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(it.value)
                                        SecurityType.Tier2 -> vaultCipher.decryptWithSecretKey(it.value)
                                        SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(it.value)
                                    },
                                )
                            }
                        }
                    },
                )
            }

            is ItemContent.PaymentCard -> {
                content.copy(
                    cardNumber = content.cardNumber?.let {
                        when (it) {
                            is SecretField.ClearText -> it
                            is SecretField.Encrypted -> {
                                SecretField.ClearText(
                                    when (securityType) {
                                        SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(it.value)
                                        SecurityType.Tier2 -> vaultCipher.decryptWithSecretKey(it.value)
                                        SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(it.value)
                                    },
                                )
                            }
                        }
                    },
                    expirationDate = content.expirationDate?.let {
                        when (it) {
                            is SecretField.ClearText -> it
                            is SecretField.Encrypted -> {
                                SecretField.ClearText(
                                    when (securityType) {
                                        SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(it.value)
                                        SecurityType.Tier2 -> vaultCipher.decryptWithSecretKey(it.value)
                                        SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(it.value)
                                    },
                                )
                            }
                        }
                    },
                    securityCode = content.securityCode?.let {
                        when (it) {
                            is SecretField.ClearText -> it
                            is SecretField.Encrypted -> {
                                SecretField.ClearText(
                                    when (securityType) {
                                        SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(it.value)
                                        SecurityType.Tier2 -> vaultCipher.decryptWithSecretKey(it.value)
                                        SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(it.value)
                                    },
                                )
                            }
                        }
                    },
                )
            }

            is ItemContent.Unknown -> {
                unknownItemEncryptionMapper.decrypt(
                    rawJson = content.rawJson,
                    securityType = securityType,
                    vaultCipher = vaultCipher,
                    decryptSecretFields = true,
                )
            }
        }
    }

    fun encryptSecretFields(
        content: ItemContent,
        encryptionKey: ByteArray,
    ): ItemContent {
        return when (content) {
            is ItemContent.Login -> {
                content.copy(
                    password = content.password?.let {
                        when (it) {
                            is SecretField.Encrypted -> it
                            is SecretField.ClearText -> {
                                if (it.value.isBlank()) {
                                    null
                                } else {
                                    SecretField.Encrypted(
                                        encrypt(key = encryptionKey, data = it.value),
                                    )
                                }
                            }
                        }
                    },
                )
            }

            is ItemContent.SecureNote -> {
                content.copy(
                    text = content.text?.let {
                        when (it) {
                            is SecretField.Encrypted -> it
                            is SecretField.ClearText -> {
                                if (it.value.isBlank()) {
                                    null
                                } else {
                                    SecretField.Encrypted(
                                        encrypt(key = encryptionKey, data = it.value),
                                    )
                                }
                            }
                        }
                    },
                )
            }

            is ItemContent.PaymentCard -> {
                content.copy(
                    cardNumber = content.cardNumber?.let {
                        when (it) {
                            is SecretField.Encrypted -> it
                            is SecretField.ClearText -> {
                                if (it.value.isBlank()) {
                                    null
                                } else {
                                    SecretField.Encrypted(
                                        encrypt(key = encryptionKey, data = it.value),
                                    )
                                }
                            }
                        }
                    },
                    expirationDate = content.expirationDate?.let {
                        when (it) {
                            is SecretField.Encrypted -> it
                            is SecretField.ClearText -> {
                                if (it.value.isBlank()) {
                                    null
                                } else {
                                    SecretField.Encrypted(
                                        encrypt(key = encryptionKey, data = it.value),
                                    )
                                }
                            }
                        }
                    },
                    securityCode = content.securityCode?.let {
                        when (it) {
                            is SecretField.Encrypted -> it
                            is SecretField.ClearText -> {
                                if (it.value.isBlank()) {
                                    null
                                } else {
                                    SecretField.Encrypted(
                                        encrypt(key = encryptionKey, data = it.value),
                                    )
                                }
                            }
                        }
                    },
                )
            }

            is ItemContent.Unknown -> {
                unknownItemEncryptionMapper.encryptSecretFields(
                    rawJson = content.rawJson,
                    encryptionKey = encryptionKey,
                )
            }
        }
    }
}