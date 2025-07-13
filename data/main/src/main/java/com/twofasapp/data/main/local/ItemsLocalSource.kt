/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.local

import com.twofasapp.data.main.local.dao.ItemsDao
import com.twofasapp.data.main.local.model.CloudMergeEntity
import com.twofasapp.data.main.local.model.ItemEntity
import kotlinx.coroutines.flow.Flow

internal class ItemsLocalSource(
    private val itemsDao: ItemsDao,
) {
    fun observe(vaultId: String): Flow<List<ItemEntity>> {
        return itemsDao.observe(vaultId)
    }

    fun observeDeleted(): Flow<List<ItemEntity>> {
        return itemsDao.observeDeleted()
    }

    suspend fun getLogin(id: String): ItemEntity {
        return itemsDao.get(id)
    }

    suspend fun getLogins(ids: List<String>): List<ItemEntity> {
        return itemsDao.get(ids)
    }

    suspend fun getLogins(): List<ItemEntity> {
        return itemsDao.get()
    }

    suspend fun countLogins(): Int {
        return itemsDao.count()
    }

    suspend fun getLoginsWithDeleted(): List<ItemEntity> {
        return itemsDao.getWithDeleted()
    }

    suspend fun getLoginsDeleted(ids: List<String>): List<ItemEntity> {
        return itemsDao.getDeleted(ids)
    }

    suspend fun saveLogin(entity: ItemEntity) {
        itemsDao.save(entity)
    }

    suspend fun saveLogins(entities: List<ItemEntity>) {
        itemsDao.saveInTransaction(entities)
    }

    suspend fun delete(ids: List<String>) {
        itemsDao.delete(ids)
    }

    suspend fun deleteAll() {
        itemsDao.deleteAll()
    }

    suspend fun executeCloudMerge(cloudMerge: CloudMergeEntity) {
        itemsDao.executeCloudMerge(cloudMerge)
    }

    suspend fun getMostRecentUpdatedAt(): Long {
        return itemsDao.getMostRecentUpdateTime() ?: 0
    }
}