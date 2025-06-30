/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.local

import com.twofasapp.data.main.local.dao.LoginsDao
import com.twofasapp.data.main.local.model.CloudMergeEntity
import com.twofasapp.data.main.local.model.LoginEntity
import com.twofasapp.data.main.local.model.UsernameFrequencyEntity
import kotlinx.coroutines.flow.Flow

internal class LoginsLocalSource(
    private val loginsDao: LoginsDao,
) {
    fun observe(vaultId: String): Flow<List<LoginEntity>> {
        return loginsDao.observe(vaultId)
    }

    fun observeDeleted(): Flow<List<LoginEntity>> {
        return loginsDao.observeDeleted()
    }

    suspend fun getLogin(id: String): LoginEntity {
        return loginsDao.get(id)
    }

    suspend fun getLogins(ids: List<String>): List<LoginEntity> {
        return loginsDao.get(ids)
    }

    suspend fun getLogins(): List<LoginEntity> {
        return loginsDao.get()
    }

    suspend fun countLogins(): Int {
        return loginsDao.count()
    }

    suspend fun getLoginsWithDeleted(): List<LoginEntity> {
        return loginsDao.getWithDeleted()
    }

    suspend fun getLoginsDeleted(ids: List<String>): List<LoginEntity> {
        return loginsDao.getDeleted(ids)
    }

    suspend fun saveLogin(entity: LoginEntity) {
        loginsDao.save(entity)
    }

    suspend fun saveLogins(entities: List<LoginEntity>) {
        loginsDao.saveInTransaction(entities)
    }

    suspend fun delete(ids: List<String>) {
        loginsDao.delete(ids)
    }

    suspend fun deleteAll() {
        loginsDao.deleteAll()
    }

    suspend fun getUsernamesFrequency(): List<UsernameFrequencyEntity> {
        return loginsDao.getUsernamesFrequency()
    }

    suspend fun executeCloudMerge(cloudMerge: CloudMergeEntity) {
        loginsDao.executeCloudMerge(cloudMerge)
    }

    suspend fun getMostRecentUpdatedAt(): Long {
        return loginsDao.getMostRecentUpdateTime() ?: 0
    }
}