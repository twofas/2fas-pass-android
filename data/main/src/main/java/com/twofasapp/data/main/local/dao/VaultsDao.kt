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
import com.twofasapp.data.main.local.model.VaultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultsDao {
    @Query("SELECT * FROM vaults")
    fun observe(): Flow<List<VaultEntity>>

    @Query("SELECT * FROM vaults WHERE id == :id")
    fun observe(id: String): Flow<VaultEntity>

    @Query("SELECT * FROM vaults")
    suspend fun get(): List<VaultEntity>

    @Query("SELECT * FROM vaults WHERE id == :id")
    suspend fun get(id: String): VaultEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: VaultEntity)

    @Query("DELETE FROM vaults WHERE id IN (:ids)")
    suspend fun delete(ids: List<String>)

    @Query("DELETE FROM vaults")
    suspend fun deleteAll()

    @Query("UPDATE vaults SET updated_at = :timestamp WHERE id == :id")
    suspend fun updateLastModificationTime(id: String, timestamp: Long)
}