/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.local

import com.twofasapp.data.main.local.dao.TagsDao
import com.twofasapp.data.main.local.model.TagEntity
import kotlinx.coroutines.flow.Flow

internal class TagsLocalSource(
    private val dao: TagsDao,
) {
    fun observe(vaultId: String): Flow<List<TagEntity>> {
        return dao.observe(vaultId)
    }

    suspend fun getTags(): List<TagEntity> {
        return dao.getAll()
    }

    suspend fun getTags(vaultId: String): List<TagEntity> {
        return dao.getAll(vaultId)
    }

    suspend fun saveTags(entities: List<TagEntity>) {
        dao.save(entities)
    }

    suspend fun deleteTags(ids: List<String>) {
        dao.deleteByIds(ids)
    }

    suspend fun deleteAll(vaultId: String) {
        dao.deleteAll(vaultId)
    }

    suspend fun getMostRecentUpdatedAt(): Long {
        return dao.getMostRecentUpdateTime() ?: 0
    }
}