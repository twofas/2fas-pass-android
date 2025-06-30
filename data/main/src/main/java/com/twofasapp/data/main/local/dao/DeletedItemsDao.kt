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
import androidx.room.Transaction
import com.twofasapp.data.main.local.model.DeletedItemEntity

@Dao
interface DeletedItemsDao {
    @Query("SELECT * FROM deleted_items WHERE vault_id == :vaultId")
    suspend fun get(vaultId: String): List<DeletedItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entities: List<DeletedItemEntity>)

    @Transaction
    suspend fun saveInTransaction(entities: List<DeletedItemEntity>) {
        entities.chunked(500).forEach { chunk ->
            save(chunk)
        }
    }

    @Query("DELETE FROM deleted_items WHERE id IN (:ids)")
    suspend fun delete(ids: List<String>)

    @Query("DELETE FROM deleted_items WHERE vault_id == :vaultId")
    suspend fun deleteAll(vaultId: String)
}