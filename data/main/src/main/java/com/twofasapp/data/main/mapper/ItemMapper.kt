/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.DeletedItem
import com.twofasapp.core.common.domain.SecretField.ClearText
import com.twofasapp.core.common.domain.SecretField.Encrypted
import com.twofasapp.core.common.domain.WifiSecurityType
import com.twofasapp.core.common.domain.clearText
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.domain.encryptedText
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.common.domain.items.ItemEncrypted
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.core.common.ktx.removeWhitespace
import com.twofasapp.data.main.local.model.ItemEntity
import com.twofasapp.data.main.remote.model.ItemContentJson
import com.twofasapp.data.main.remote.model.ItemJson
import com.twofasapp.data.main.remote.model.vaultbackup.LoginJson
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

internal class ItemMapper(
    private val jsonSerializer: Json,
    private val securityTypeMapper: ItemSecurityTypeMapper,
    private val iconTypeMapper: IconTypeMapper,
    private val itemSecurityTypeMapper: ItemSecurityTypeMapper,
    private val uriMapper: ItemUriMapper,
) {
    fun mapToDomain(entity: ItemEntity): ItemEncrypted {
        return with(entity) {
            ItemEncrypted(
                id = id,
                vaultId = vaultId,
                createdAt = createdAt,
                updatedAt = updatedAt,
                deletedAt = deletedAt,
                deleted = deleted,
                securityType = securityTypeMapper.mapToDomainFromEntity(securityType),
                contentType = ItemContentType.fromKey(contentType),
                content = content,
                tagIds = tagIds.orEmpty(),
            )
        }
    }

    fun mapToEntity(domain: ItemEncrypted): ItemEntity {
        return with(domain) {
            ItemEntity(
                id = id,
                vaultId = vaultId,
                createdAt = createdAt,
                updatedAt = updatedAt,
                deletedAt = deletedAt,
                deleted = deleted,
                securityType = securityTypeMapper.mapToEntity(securityType),
                contentType = contentType.key,
                contentVersion = contentType.version,
                content = content,
                tagIds = tagIds.ifEmpty { null },
            )
        }
    }

    fun mapToDomain(
        json: ItemJson,
        vaultId: String,
        tagIds: List<String>?,
        hasSecretFieldsEncrypted: Boolean,
    ): Item {
        return with(json) {
            val itemContentType = ItemContentType.fromKey(contentType)
            Item(
                id = id,
                vaultId = vaultId,
                createdAt = createdAt,
                updatedAt = updatedAt,
                deletedAt = null,
                deleted = false,
                securityType = securityTypeMapper.mapToDomainFromEntity(securityType),
                contentType = itemContentType,
                content = mapItemContentToDomain(
                    contentType = itemContentType,
                    contentJson = content,
                    hasSecretFieldsEncrypted = hasSecretFieldsEncrypted,
                ),
                tagIds = tagIds.orEmpty(),
            )
        }
    }

    fun mapToJson(item: Item): ItemJson {
        return mapToJson(
            item = item,
            content = mapItemContentToJson(
                content = item.content,
                wifiContentMapper = { mapToWifiJsonElement(it) }
            )
        )
    }

    fun mapToBrowserJson(item: Item): ItemJson {
        return mapToJson(
            item = item,
            content = mapItemContentToJson(
                content = item.content,
                wifiContentMapper = { mapToBrowserWifiJsonElement(it) }
            )
        )
    }

    private fun mapToJson(item: Item, content: JsonElement): ItemJson {
        return ItemJson(
            id = item.id,
            vaultId = item.vaultId,
            createdAt = item.createdAt,
            updatedAt = item.updatedAt,
            securityType = item.securityType.let(itemSecurityTypeMapper::mapToJson),
            contentType = item.contentType.key,
            contentVersion = item.contentType.version,
            content = content,
            tags = item.tagIds.ifEmpty { null },
        )
    }

    fun mapToDeletedItem(entity: ItemEntity): DeletedItem {
        return with(entity) {
            DeletedItem(
                id = id,
                vaultId = vaultId,
                type = entity.contentType,
                deletedAt = deletedAt ?: 0,
            )
        }
    }

    private fun mapItemContentToDomain(
        contentType: ItemContentType,
        contentJson: JsonElement,
        hasSecretFieldsEncrypted: Boolean,
    ): ItemContent {
        return when (contentType) {
            ItemContentType.Login -> {
                val content = jsonSerializer.decodeFromJsonElement(
                    ItemContentJson.Login.serializer(),
                    contentJson
                )

                ItemContent.Login(
                    name = content.name.orEmpty(),
                    username = content.username,
                    password = content.password?.let {
                        if (hasSecretFieldsEncrypted) {
                            Encrypted(EncryptedBytes(it.decodeBase64()))
                        } else {
                            ClearText(it)
                        }
                    },
                    uris = content.uris.map { uriMapper.mapToDomain(it) },
                    iconType = iconTypeMapper.mapToDomainFromJson(content.iconType),
                    iconUriIndex = content.iconUriIndex,
                    labelText = content.labelText,
                    labelColor = content.labelColor,
                    customImageUrl = content.customImageUrl,
                    notes = content.notes,
                )
            }

            ItemContentType.SecureNote -> {
                val content =
                    jsonSerializer.decodeFromJsonElement(
                        ItemContentJson.SecureNote.serializer(),
                        contentJson
                    )

                ItemContent.SecureNote(
                    name = content.name.orEmpty(),
                    text = content.text?.let {
                        if (hasSecretFieldsEncrypted) {
                            Encrypted(EncryptedBytes(it.decodeBase64()))
                        } else {
                            ClearText(it)
                        }
                    },
                    additionalInfo = content.additionalInfo,
                )
            }

            ItemContentType.PaymentCard -> {
                val content =
                    jsonSerializer.decodeFromJsonElement(
                        ItemContentJson.PaymentCard.serializer(),
                        contentJson
                    )

                ItemContent.PaymentCard(
                    name = content.name.orEmpty(),
                    cardHolder = content.cardHolder,
                    cardNumber = content.cardNumber?.let {
                        if (hasSecretFieldsEncrypted) {
                            Encrypted(EncryptedBytes(it.decodeBase64()))
                        } else {
                            ClearText(it.removeWhitespace())
                        }
                    },
                    expirationDate = content.expirationDate?.let {
                        if (hasSecretFieldsEncrypted) {
                            Encrypted(EncryptedBytes(it.decodeBase64()))
                        } else {
                            ClearText(it.removeWhitespace())
                        }
                    },
                    securityCode = content.securityCode?.let {
                        if (hasSecretFieldsEncrypted) {
                            Encrypted(EncryptedBytes(it.decodeBase64()))
                        } else {
                            ClearText(it.removeWhitespace())
                        }
                    },
                    cardNumberMask = content.cardNumberMask?.removeWhitespace(),
                    cardIssuer = ItemContent.PaymentCard.Issuer.fromCode(content.cardIssuer),
                    notes = content.notes,
                )
            }

            is ItemContentType.Unknown -> ItemContent.Unknown(
                rawJson = jsonSerializer.encodeToString(
                    contentJson,
                ),
            )

            ItemContentType.Wifi -> {
                val content =
                    jsonSerializer.decodeFromJsonElement(
                        ItemContentJson.Wifi.serializer(),
                        contentJson
                    )

                ItemContent.Wifi(
                    name = content.name.orEmpty(),
                    password = content.password?.let {
                        if (hasSecretFieldsEncrypted) {
                            Encrypted(EncryptedBytes(it.decodeBase64()))
                        } else {
                            ClearText(it)
                        }
                    },
                    ssid = content.ssid,
                    securityType = WifiSecurityType.fromValue(content.securityType),
                    hidden = content.hidden ?: false,
                    notes = content.notes,
                )
            }

            ItemContentType.Passkey -> {
                val content = jsonSerializer.decodeFromJsonElement(
                    ItemContentJson.Passkey.serializer(),
                    contentJson
                )

                ItemContent.Passkey(
                    name = content.name.orEmpty(),
                    privateKey = content.privateKey?.let {
                        if (hasSecretFieldsEncrypted) {
                            Encrypted(EncryptedBytes(it.decodeBase64()))
                        } else {
                            ClearText(it)
                        }
                    },
                    userHandle = content.userHandle,
                    credentialId = content.credentialId,
                    rpId = content.rpId,
                    notes = content.notes
                )
            }
        }
    }

    private fun mapItemContentToJson(
        content: ItemContent,
        wifiContentMapper: (ItemContent.Wifi) -> JsonElement
    ): JsonElement {
        return when (content) {
            is ItemContent.Login -> {
                jsonSerializer.encodeToJsonElement(
                    ItemContentJson.Login(
                        name = content.name,
                        username = content.username,
                        password = when (content.password) {
                            is ClearText -> content.password.clearText
                            is Encrypted -> content.password.encryptedText
                            null -> null
                        },
                        uris = content.uris.map { uriMapper.mapToItemContentJson(it) },
                        iconType = iconTypeMapper.mapToJson(content.iconType),
                        iconUriIndex = content.iconUriIndex,
                        labelText = content.labelText,
                        labelColor = content.labelColor,
                        customImageUrl = content.customImageUrl,
                        notes = content.notes,
                    ),
                )
            }

            is ItemContent.SecureNote -> {
                jsonSerializer.encodeToJsonElement(
                    ItemContentJson.SecureNote(
                        name = content.name,
                        text = when (content.text) {
                            is ClearText -> content.text.clearText
                            is Encrypted -> content.text.encryptedText
                            null -> null
                        },
                        additionalInfo = content.additionalInfo,
                    ),
                )
            }

            is ItemContent.PaymentCard -> {
                jsonSerializer.encodeToJsonElement(
                    ItemContentJson.PaymentCard(
                        name = content.name,
                        cardHolder = content.cardHolder,
                        cardNumber = when (content.cardNumber) {
                            is ClearText -> content.cardNumber.clearText.removeWhitespace()
                            is Encrypted -> content.cardNumber.encryptedText
                            null -> null
                        },
                        expirationDate = when (content.expirationDate) {
                            is ClearText -> content.expirationDate.clearText.removeWhitespace()
                            is Encrypted -> content.expirationDate.encryptedText
                            null -> null
                        },
                        securityCode = when (content.securityCode) {
                            is ClearText -> content.securityCode.clearText.removeWhitespace()
                            is Encrypted -> content.securityCode.encryptedText
                            null -> null
                        },
                        cardNumberMask = content.cardNumberMask?.removeWhitespace(),
                        cardIssuer = content.cardIssuer?.code,
                        notes = content.notes,
                    ),
                )
            }

            is ItemContent.Unknown -> {
                jsonSerializer.parseToJsonElement(content.rawJson)
            }

            is ItemContent.Wifi -> wifiContentMapper(content)
            is ItemContent.Passkey -> {
                jsonSerializer.encodeToJsonElement(
                    ItemContentJson.Passkey(
                        name = content.name,
                        privateKey = when (content.privateKey) {
                            is ClearText -> content.privateKey.clearText
                            is Encrypted -> content.privateKey.encryptedText
                            null -> null
                        },
                        userHandle = content.userHandle,
                        credentialId = content.credentialId,
                        rpId = content.rpId,
                        notes = content.notes,
                    ),
                )
            }
        }
    }

    private fun mapToWifiJsonElement(content: ItemContent.Wifi): JsonElement {
        return jsonSerializer.encodeToJsonElement(
            ItemContentJson.Wifi(
                name = content.name,
                ssid = content.ssid,
                password = when (content.password) {
                    is ClearText -> content.password.clearText
                    is Encrypted -> content.password.encryptedText
                    null -> null
                },
                securityType = content.securityType.value,
                hidden = content.hidden,
                notes = content.notes,
            ),
        )
    }

    private fun mapToBrowserWifiJsonElement(content: ItemContent.Wifi): JsonElement {
        return jsonSerializer.encodeToJsonElement(
            ItemContentJson.BrowserWifi(
                name = content.name,
                ssid = content.ssid,
                password = when (content.password) {
                    is ClearText -> content.password.clearText
                    is Encrypted -> content.password.encryptedText
                    null -> null
                },
                securityType = content.securityType.value,
                hidden = content.hidden,
                notes = content.notes,
            ),
        )
    }

    fun mapToJsonV1(domain: Item, deviceId: String? = null): LoginJson {
        val content = domain.content as ItemContent.Login

        return LoginJson(
            id = domain.id,
            deviceId = deviceId,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            name = content.name,
            username = content.username,
            password = content.password?.let { (it as ClearText).value },
            securityType = securityTypeMapper.mapToJson(domain.securityType),
            uris = content.uris.map { uriMapper.mapToJson(it) },
            iconType = iconTypeMapper.mapToJson(content.iconType),
            iconUriIndex = content.iconUriIndex,
            labelText = content.labelText,
            labelColor = content.labelColor,
            customImageUrl = content.customImageUrl,
            notes = content.notes,
            tags = domain.tagIds.ifEmpty { null },
        )
    }

    fun mapToDomainFromV1(json: LoginJson, vaultId: String): Item {
        return Item.Empty.copy(
            id = json.id,
            vaultId = vaultId,
            createdAt = json.createdAt,
            updatedAt = json.updatedAt,
            securityType = securityTypeMapper.mapToDomainFromJson(json.securityType),
            tagIds = json.tags.orEmpty(),
            contentType = ItemContentType.Login,
            content = ItemContent.Login(
                name = json.name,
                username = json.username,
                password = json.password?.let { ClearText(it) },
                uris = json.uris.map { uriMapper.mapToDomain(it) },
                iconType = iconTypeMapper.mapToDomainFromJson(json.iconType),
                iconUriIndex = json.iconUriIndex,
                labelText = json.labelText,
                labelColor = json.labelColor,
                customImageUrl = json.customImageUrl,
                notes = json.notes,
            ),
        )
    }
}