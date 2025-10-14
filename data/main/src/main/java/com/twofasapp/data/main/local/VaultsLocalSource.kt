/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.local

import com.twofasapp.data.main.local.dao.VaultsDao
import com.twofasapp.data.main.local.model.VaultEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

internal class VaultsLocalSource(
    private val dao: VaultsDao,
) {
    fun observe(): Flow<List<VaultEntity>> {
        return dao.observe()
    }

    fun observe(id: String): Flow<VaultEntity> {
        return dao.observe(id)
    }

    suspend fun save(entity: VaultEntity): VaultEntity {
        dao.save(entity)
        return dao.get(entity.id)
    }

    suspend fun get(): List<VaultEntity> {
        return dao.get()
    }

    suspend fun get(id: String): VaultEntity {
        return dao.get(id)
    }

    suspend fun count(): Int {
        return dao.observe().first().count()
    }

    suspend fun delete(ids: List<String>) {
        dao.delete(ids)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }

    suspend fun updateLastModificationTime(id: String, timestamp: Long) {
        if (timestamp > dao.get(id).updatedAt) {
            dao.updateLastModificationTime(id, timestamp)
        }
    }
}