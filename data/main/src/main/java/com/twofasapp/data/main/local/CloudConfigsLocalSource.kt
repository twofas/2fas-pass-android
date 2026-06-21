/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.local

import com.twofasapp.data.main.local.dao.CloudConfigsDao
import com.twofasapp.data.main.local.model.CloudConfigEntity
import kotlinx.coroutines.flow.Flow

class CloudConfigsLocalSource(
    private val dao: CloudConfigsDao,
) {
    fun observeAll(): Flow<List<CloudConfigEntity>> {
        return dao.observeAll()
    }

    fun observe(id: String): Flow<CloudConfigEntity?> {
        return dao.observe(id)
    }

    suspend fun getAll(): List<CloudConfigEntity> {
        return dao.getAll()
    }

    suspend fun get(id: String): CloudConfigEntity? {
        return dao.get(id)
    }

    suspend fun save(entity: CloudConfigEntity) {
        dao.save(entity)
    }

    suspend fun updateStatus(id: String, status: String, errorCode: String?) {
        dao.updateStatus(id, status, errorCode)
    }

    suspend fun updateLastSyncTime(id: String, lastSyncTime: Long) {
        dao.updateSyncedAt(id, lastSyncTime)
    }

    suspend fun delete(id: String) {
        dao.delete(id)
    }
}