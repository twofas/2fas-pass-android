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
import com.twofasapp.core.common.domain.clearTextOrNull
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.common.domain.items.ItemEncrypted
import com.twofasapp.data.main.local.model.ItemEntity
import com.twofasapp.data.main.remote.model.ItemContentJson
import com.twofasapp.data.main.remote.model.ItemJson
import com.twofasapp.data.main.remote.model.vaultbackup.LoginJson
import kotlinx.serialization.json.Json

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

    fun mapToDomain(json: ItemJson, vaultId: String, tagIds: List<String>?): Item {
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
                content = mapItemContentToDomain(contentType = contentType, contentJson = content),
                tagIds = tagIds.orEmpty(),
            )
        }
    }

    fun mapToJson(item: Item, deviceId: String? = null): ItemJson? {
        return ItemJson(
            id = item.id,
            deviceId = deviceId,
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
        contentJson: String,
    ): ItemContent {
        return when (contentType) {
            ItemContentType.Login.key -> {
                val content = jsonSerializer.decodeFromString(ItemContentJson.Login.serializer(), contentJson)

                ItemContent.Login(
                    name = content.name,
                    username = content.username,
                    password = content.password?.let { SecretField.ClearText(it) },
                    uris = content.uris.map { uriMapper.mapToDomain(it) },
                    iconType = iconTypeMapper.mapToDomainFromJson(content.iconType),
                    iconUriIndex = content.iconUriIndex,
                    labelText = content.labelText,
                    labelColor = content.labelColor,
                    customImageUrl = content.customImageUrl,
                    notes = content.notes,
                )
            }

            // TODO: Uncomment when SecureNote is implemented
//            ItemContentType.SecureNote.key -> {
//                val content = jsonSerializer.decodeFromString(ItemContentJson.SecureNote.serializer(), contentJson)
//
//                ItemContent.SecureNote(
//                    name = content.name,
//                    text = content.text?.let { SecretField.ClearText(it) },
//                )
//            }

            else -> {
                ItemContent.Unknown(rawJson = contentJson)
            }
        }
    }

    private fun mapItemContentToJson(
        content: ItemContent,
    ): String {
        return when (content) {
            is ItemContent.Login -> {
                jsonSerializer.encodeToString(
                    ItemContentJson.Login(
                        name = content.name,
                        username = content.username,
                        password = content.password.clearTextOrNull,
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
                jsonSerializer.encodeToString(
                    ItemContentJson.SecureNote(
                        name = content.name,
                        text = content.text.clearTextOrNull,
                    ),
                )
            }

            is ItemContent.Unknown -> {
                content.rawJson
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