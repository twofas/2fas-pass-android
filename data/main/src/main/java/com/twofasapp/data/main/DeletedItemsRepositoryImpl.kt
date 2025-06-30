/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.domain.DeletedItem
import com.twofasapp.data.main.local.DeletedItemsLocalSource
import com.twofasapp.data.main.mapper.DeletedItemsMapper
import kotlinx.coroutines.withContext

internal class DeletedItemsRepositoryImpl(
    private val dispatchers: Dispatchers,
    private val localSource: DeletedItemsLocalSource,
    private val deletedItemsMapper: DeletedItemsMapper,
) : DeletedItemsRepository {

    override suspend fun getDeletedItems(vaultId: String): List<DeletedItem> {
        return withContext(dispatchers.io) {
            localSource.getDeletedItems(vaultId).map { it.let(deletedItemsMapper::mapToDomain) }.sortedByDescending { it.deletedAt }
        }
    }

    override suspend fun saveDeletedItems(entities: List<DeletedItem>) {
        withContext(dispatchers.io) {
            localSource.saveDeletedItems(entities.map { it.let(deletedItemsMapper::mapToEntity) })
        }
    }

    override suspend fun clearDeletedItems(ids: List<String>) {
        withContext(dispatchers.io) {
            localSource.clearDeletedItems(ids)
        }
    }

    override suspend fun clearAll(vaultId: String) {
        withContext(dispatchers.io) {
            localSource.clearAll(vaultId)
        }
    }
}