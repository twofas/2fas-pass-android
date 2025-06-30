/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.twofasapp.data.main.local.model.ConnectedBrowserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectedBrowsersDao {
    @Query("SELECT * FROM connected_browsers")
    fun observeAll(): Flow<List<ConnectedBrowserEntity>>

    @Query("SELECT * FROM connected_browsers")
    suspend fun getAll(): List<ConnectedBrowserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: ConnectedBrowserEntity)

    @Query("DELETE FROM connected_browsers WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("SELECT COUNT(id) FROM connected_browsers")
    suspend fun count(): Int
}