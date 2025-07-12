/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.DeletedItem
import com.twofasapp.core.common.domain.ItemEncrypted
import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.data.main.local.model.ItemEntity
import com.twofasapp.data.main.local.model.LoginEntity
import com.twofasapp.data.main.remote.model.LoginJson

internal class LoginMapper(
    private val iconTypeMapper: LoginIconTypeMapper,
    private val securityTypeMapper: LoginSecurityTypeMapper,
    private val uriMapper: LoginUriMapper,
) {

//    @Deprecated("Legacy")
//    fun mapToDomain(entity: LoginEntity): ItemEncrypted {
//        return with(entity) {
//            ItemEncrypted(
//                id = id,
//                vaultId = vaultId,
//                name = name,
//                username = username,
//                password = password,
//                securityType = securityTypeMapper.mapToDomainFromEntity(securityType),
//                uris = uris.orEmpty().map { uriMapper.mapToDomain(it) },
//                iconType = iconTypeMapper.mapToDomainFromEntity(iconType),
//                iconUriIndex = iconUriIndex,
//                customImageUrl = customImageUrl,
//                labelText = labelText,
//                labelColor = labelColor,
//                notes = notes,
//                tags = tags.orEmpty(),
//                deleted = deleted,
//                createdAt = createdAt,
//                updatedAt = updatedAt,
//                deletedAt = deletedAt,
//            )
//        }
//    }

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

//    fun mapToEntity(domain: ItemEncrypted): LoginEntity {
//        return with(domain) {
//            LoginEntity(
//                id = id,
//                vaultId = vaultId,
//                createdAt = createdAt,
//                updatedAt = updatedAt,
//                deletedAt = deletedAt,
//                deleted = deleted,
//                name = name,
//                username = username,
//                password = password,
//                securityType = securityTypeMapper.mapToEntity(securityType),
//                uris = uris.filter { it.text.bytes.isNotEmpty() }.map { uriMapper.mapToEntity(it) },
//                iconType = iconTypeMapper.mapToEntity(iconType),
//                iconUriIndex = iconUriIndex,
//                customImageUrl = customImageUrl,
//                labelText = labelText,
//                labelColor = labelColor,
//                notes = notes,
//                tags = tags,
//            )
//        }
//    }

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
                tagIds = tagIds,
            )
        }
    }

    fun mapToJson(domain: Login, deviceId: String? = null): LoginJson {
        return with(domain) {
            LoginJson(
                id = id,
                deviceId = deviceId,
                createdAt = createdAt,
                updatedAt = updatedAt,
                name = name,
                username = username,
                password = password?.let { (it as SecretField.Visible).value },
                securityType = securityTypeMapper.mapToJson(securityType),
                uris = uris.map { uriMapper.mapToJson(it) },
                iconType = iconTypeMapper.mapToJson(iconType),
                iconUriIndex = iconUriIndex,
                labelText = labelText,
                labelColor = labelColor,
                customImageUrl = customImageUrl,
                notes = notes,
                tags = tagIds.ifEmpty { null },
            )
        }
    }

    fun mapToDomain(json: LoginJson, vaultId: String): Login {
        return with(json) {
            Login(
                id = id,
                vaultId = vaultId,
                name = name,
                username = username,
                password = password?.let { SecretField.Visible(it) },
                securityType = securityTypeMapper.mapToDomainFromJson(securityType),
                uris = uris.map { uriMapper.mapToDomain(it) },
                iconType = iconTypeMapper.mapToDomainFromJson(iconType),
                iconUriIndex = iconUriIndex,
                labelText = labelText,
                labelColor = labelColor,
                customImageUrl = customImageUrl,
                notes = notes,
                tagIds = tags.orEmpty(),
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
        }
    }

    fun mapToJson(domains: List<Login>): List<LoginJson> {
        return domains.map { mapToJson(it) }
    }

    fun mapToDeletedItem(entity: LoginEntity): DeletedItem {
        return with(entity) {
            DeletedItem(
                id = id,
                vaultId = vaultId,
                type = "login",
                deletedAt = deletedAt ?: 0,
            )
        }
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