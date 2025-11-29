/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.DeletedItem
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.clearText
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.domain.encryptedText
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.common.domain.items.ItemEncrypted
import com.twofasapp.core.common.ktx.decodeBase64
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

    fun mapToDomain(json: ItemJson, vaultId: String, tagIds: List<String>?, hasSecretFieldsEncrypted: Boolean): Item {
        return with(json) {
            Item(
                id = id,
                vaultId = vaultId,
                createdAt = createdAt,
                updatedAt = updatedAt,
                deletedAt = null,
                deleted = false,
                securityType = securityTypeMapper.mapToDomainFromEntity(securityType),
                contentType = ItemContentType.fromKey(contentType),
                content = mapItemContentToDomain(contentType = contentType, contentJson = content, hasSecretFieldsEncrypted = hasSecretFieldsEncrypted),
                tagIds = tagIds.orEmpty(),
            )
        }
    }

    fun mapToJson(item: Item): ItemJson {
        return ItemJson(
            id = item.id,
            vaultId = item.vaultId,
            createdAt = item.createdAt,
            updatedAt = item.updatedAt,
            securityType = item.securityType.let(itemSecurityTypeMapper::mapToJson),
            contentType = item.contentType.key,
            contentVersion = item.contentType.version,
            content = mapItemContentToJson(item.content),
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
        contentType: String,
        contentJson: JsonElement,
        hasSecretFieldsEncrypted: Boolean,
    ): ItemContent {
        return when (contentType) {
            ItemContentType.Login.key -> {
                val content = jsonSerializer.decodeFromJsonElement(ItemContentJson.Login.serializer(), contentJson)

                ItemContent.Login(
                    name = content.name,
                    username = content.username,
                    password = content.password?.let {
                        if (hasSecretFieldsEncrypted) {
                            SecretField.Encrypted(EncryptedBytes(it.decodeBase64()))
                        } else {
                            SecretField.ClearText(it)
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

            ItemContentType.SecureNote.key -> {
                val content = jsonSerializer.decodeFromJsonElement(ItemContentJson.SecureNote.serializer(), contentJson)

                ItemContent.SecureNote(
                    name = content.name,
                    text = content.text?.let {
                        if (hasSecretFieldsEncrypted) {
                            SecretField.Encrypted(EncryptedBytes(it.decodeBase64()))
                        } else {
                            SecretField.ClearText(it)
                        }
                    },
                )
            }

            ItemContentType.PaymentCard.key -> {
                val content = jsonSerializer.decodeFromJsonElement(ItemContentJson.PaymentCard.serializer(), contentJson)

                ItemContent.PaymentCard(
                    name = content.name,
                    cardHolder = content.cardHolder,
                    cardNumber = content.cardNumber?.let {
                        if (hasSecretFieldsEncrypted) {
                            SecretField.Encrypted(EncryptedBytes(it.decodeBase64()))
                        } else {
                            SecretField.ClearText(it)
                        }
                    },
                    expirationDate = content.expirationDate?.let {
                        if (hasSecretFieldsEncrypted) {
                            SecretField.Encrypted(EncryptedBytes(it.decodeBase64()))
                        } else {
                            SecretField.ClearText(it)
                        }
                    },
                    securityCode = content.securityCode?.let {
                        if (hasSecretFieldsEncrypted) {
                            SecretField.Encrypted(EncryptedBytes(it.decodeBase64()))
                        } else {
                            SecretField.ClearText(it)
                        }
                    },
                    cardNumberMask = content.cardNumber,
                    issuer = ItemContent.PaymentCard.Issuer.fromCode(content.cardIssuer),
                    notes = content.notes,
                )
            }

            else -> {
                ItemContent.Unknown(rawJson = jsonSerializer.encodeToString(contentJson))
            }
        }
    }

    private fun mapItemContentToJson(
        content: ItemContent,
    ): JsonElement {
        return when (content) {
            is ItemContent.Login -> {
                jsonSerializer.encodeToJsonElement(
                    ItemContentJson.Login(
                        name = content.name,
                        username = content.username,
                        password = when (content.password) {
                            is SecretField.ClearText -> content.password.clearText
                            is SecretField.Encrypted -> content.password.encryptedText
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
                            is SecretField.ClearText -> content.text.clearText
                            is SecretField.Encrypted -> content.text.encryptedText
                            null -> null
                        },
                    ),
                )
            }

            is ItemContent.PaymentCard -> {
                jsonSerializer.encodeToJsonElement(
                    ItemContentJson.PaymentCard(
                        name = content.name,
                        cardHolder = content.cardHolder,
                        cardNumber = when (content.cardNumber) {
                            is SecretField.ClearText -> content.cardNumber.clearText
                            is SecretField.Encrypted -> content.cardNumber.encryptedText
                            null -> null
                        },
                        expirationDate = when (content.expirationDate) {
                            is SecretField.ClearText -> content.expirationDate.clearText
                            is SecretField.Encrypted -> content.expirationDate.encryptedText
                            null -> null
                        },
                        securityCode = when (content.securityCode) {
                            is SecretField.ClearText -> content.securityCode.clearText
                            is SecretField.Encrypted -> content.securityCode.encryptedText
                            null -> null
                        },
                        cardNumberMask = content.cardNumberMask,
                        cardIssuer = content.issuer?.code,
                        notes = content.notes,
                    ),
                )
            }

            is ItemContent.Unknown -> {
                jsonSerializer.parseToJsonElement(content.rawJson)
            }
        }
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
            password = content.password?.let { (it as SecretField.ClearText).value },
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
                password = json.password?.let { SecretField.ClearText(it) },
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