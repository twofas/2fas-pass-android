/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.local

import com.twofasapp.data.main.local.dao.ConnectedBrowsersDao
import com.twofasapp.data.main.local.model.ConnectedBrowserEntity
import kotlinx.coroutines.flow.Flow

internal class ConnectedBrowsersLocalSource(
    private val dao: ConnectedBrowsersDao,
) {
    fun observeConnectedBrowsers(): Flow<List<ConnectedBrowserEntity>> {
        return dao.observeAll()
    }

    suspend fun getConnectedBrowsers(): List<ConnectedBrowserEntity> {
        return dao.getAll()
    }

    suspend fun saveConnectedBrowser(entity: ConnectedBrowserEntity) {
        dao.save(entity)
    }

    suspend fun deleteConnectedBrowser(id: Int) {
        dao.delete(id)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }

    suspend fun hasAnyBrowsers(): Boolean {
        return dao.count() > 0
    }
}