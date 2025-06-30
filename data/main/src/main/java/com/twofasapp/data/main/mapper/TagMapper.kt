/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.DeletedItem
import com.twofasapp.data.main.VaultCipher
import com.twofasapp.data.main.domain.Tag
import com.twofasapp.data.main.local.model.TagEntity
import com.twofasapp.data.main.remote.model.TagJson

internal class TagMapper {

    fun mapToJson(domains: List<Tag>): List<TagJson> {
        return domains.map { mapToJson(it) }
    }

    fun mapToJson(domain: Tag): TagJson {
        return with(domain) {
            TagJson(
                id = id,
                updatedAt = updatedAt,
                name = name,
                color = color,
                position = position,
            )
        }
    }

    fun mapToEntity(
        domain: Tag,
        vaultCipher: VaultCipher,
    ): TagEntity {
        return with(domain) {
            TagEntity(
                id = id,
                vaultId = vaultId,
                updatedAt = updatedAt,
                name = vaultCipher.encryptWithTrustedKey(domain.name),
                color = color,
                position = position,
            )
        }
    }

    fun mapToDomain(
        entity: TagEntity,
        vaultCipher: VaultCipher,
    ): Tag {
        return with(entity) {
            Tag(
                id = id,
                vaultId = vaultId,
                name = vaultCipher.decryptWithTrustedKey(entity.name),
                color = color,
                position = position,
                updatedAt = updatedAt,
            )
        }
    }

    fun mapToDomain(
        json: TagJson,
        vaultId: String,
    ): Tag {
        return with(json) {
            Tag(
                id = id,
                vaultId = vaultId,
                name = name,
                color = color,
                position = position,
                updatedAt = updatedAt,
            )
        }
    }

    fun mapToDeletedItem(tag: Tag, deletedAt: Long): DeletedItem {
        return with(tag) {
            DeletedItem(
                id = id,
                vaultId = vaultId,
                type = "tag",
                deletedAt = deletedAt,
            )
        }
    }
}