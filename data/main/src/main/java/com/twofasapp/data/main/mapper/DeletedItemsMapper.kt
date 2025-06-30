/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.DeletedItem
import com.twofasapp.data.main.local.model.DeletedItemEntity
import com.twofasapp.data.main.remote.model.DeletedItemJson

internal class DeletedItemsMapper {

    fun mapToDomain(entity: DeletedItemEntity): DeletedItem {
        return with(entity) {
            DeletedItem(
                id = id,
                vaultId = vaultId,
                type = type,
                deletedAt = deletedAt,
            )
        }
    }

    fun mapToEntity(domain: DeletedItem): DeletedItemEntity {
        return with(domain) {
            DeletedItemEntity(
                id = id,
                vaultId = vaultId,
                type = type,
                deletedAt = deletedAt,
            )
        }
    }

    fun mapToJson(domain: DeletedItem): DeletedItemJson {
        return with(domain) {
            DeletedItemJson(
                id = id,
                deletedAt = deletedAt,
                type = type,
            )
        }
    }

    fun mapToDomain(json: DeletedItemJson, vaultId: String): DeletedItem {
        return with(json) {
            DeletedItem(
                id = id,
                vaultId = vaultId,
                type = type,
                deletedAt = deletedAt,
            )
        }
    }

    fun mapToJson(domains: List<DeletedItem>): List<DeletedItemJson> {
        return domains.map { mapToJson(it) }
    }
}