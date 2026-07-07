/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.twofasapp.data.main.local.model.CloudConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CloudConfigsDao {
    @Query("SELECT * FROM cloud_configs ORDER BY created_at ASC")
    fun observeAll(): Flow<List<CloudConfigEntity>>

    @Query("SELECT * FROM cloud_configs ORDER BY created_at ASC")
    suspend fun getAll(): List<CloudConfigEntity>

    @Query("SELECT * FROM cloud_configs WHERE id = :id")
    suspend fun get(id: String): CloudConfigEntity?

    @Query("SELECT * FROM cloud_configs WHERE id = :id")
    fun observe(id: String): Flow<CloudConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: CloudConfigEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entities: List<CloudConfigEntity>)

    @Query("UPDATE cloud_configs SET status = :status, error_code = :errorCode WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, errorCode: String?)

    @Query("UPDATE cloud_configs SET synced_at = :syncedAt WHERE id = :id")
    suspend fun updateSyncedAt(id: String, syncedAt: Long)

    @Query("DELETE FROM cloud_configs WHERE id = :id")
    suspend fun delete(id: String)
}