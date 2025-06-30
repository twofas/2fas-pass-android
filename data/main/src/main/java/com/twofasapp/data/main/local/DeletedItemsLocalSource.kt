/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.local

import com.twofasapp.data.main.local.dao.DeletedItemsDao
import com.twofasapp.data.main.local.model.DeletedItemEntity

internal class DeletedItemsLocalSource(
    private val deletedItemsDao: DeletedItemsDao,
) {
    suspend fun getDeletedItems(vaultId: String): List<DeletedItemEntity> {
        return deletedItemsDao.get(vaultId)
    }

    suspend fun saveDeletedItems(entities: List<DeletedItemEntity>) {
        if (entities.size > 500) {
            deletedItemsDao.saveInTransaction(entities)
        } else {
            deletedItemsDao.save(entities)
        }
    }

    suspend fun clearDeletedItems(entities: List<String>) {
        deletedItemsDao.delete(entities)
    }

    suspend fun clearAll(vaultId: String) {
        deletedItemsDao.deleteAll(vaultId)
    }
}