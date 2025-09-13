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
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemEncrypted
import com.twofasapp.data.main.local.model.ItemEntity
import com.twofasapp.data.main.remote.model.deprecated.LoginJson

internal class ItemMapper(
    private val securityTypeMapper: ItemSecurityTypeMapper,
    private val iconTypeMapper: IconTypeMapper,
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
                contentType = contentType,
                contentVersion = contentVersion,
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
                contentType = contentType,
                contentVersion = contentVersion,
                content = content,
                tagIds = tagIds.ifEmpty { null },
            )
        }
    }

    /**
     * Will be removed once fully migrated to v2
     */
    fun mapItemContentLoginToJson(domain: Item, deviceId: String? = null): LoginJson {
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

    /**
     * Will be removed once fully migrated to v2
     */
    fun mapItemContentLoginToDomain(json: LoginJson, vaultId: String): Item {
        return Item.Empty.copy(
            id = json.id,
            vaultId = vaultId,
            createdAt = json.createdAt,
            updatedAt = json.updatedAt,
            securityType = securityTypeMapper.mapToDomainFromJson(json.securityType),
            tagIds = json.tags.orEmpty(),
            contentType = "login",
            contentVersion = 1,
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

    fun mapToDeletedItem(entity: ItemEntity): DeletedItem {
        return with(entity) {
            DeletedItem(
                id = id,
                vaultId = vaultId,
                type = "login",
                deletedAt = deletedAt ?: 0,
            )
        }
    }
}