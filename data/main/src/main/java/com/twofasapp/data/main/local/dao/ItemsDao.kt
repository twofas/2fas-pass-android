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
import com.twofasapp.data.main.local.model.CloudMergeEntity
import com.twofasapp.data.main.local.model.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemsDao {
    @Query("SELECT * FROM items WHERE vault_id == :vaultId AND deleted == 0")
    fun observe(vaultId: String): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WhERE deleted == 1")
    fun observeDeleted(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id == :id")
    suspend fun get(id: String): ItemEntity

    @Query("SELECT * FROM items WHERE deleted == 0")
    suspend fun get(): List<ItemEntity>

    @Query("SELECT COUNT(id) FROM items WHERE deleted == 0")
    suspend fun count(): Int

    @Query("SELECT * FROM items")
    suspend fun getWithDeleted(): List<ItemEntity>

    @Query("SELECT * FROM items WHERE id IN (:ids) AND deleted == 0")
    suspend fun get(ids: List<String>): List<ItemEntity>

    @Query("SELECT * FROM items WHERE id IN (:ids) AND deleted == 1")
    suspend fun getDeleted(ids: List<String>): List<ItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: ItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entities: List<ItemEntity>)

    @Query("DELETE FROM items WHERE id IN (:ids)")
    suspend fun delete(ids: List<String>)

    @Query("DELETE FROM items")
    suspend fun deleteAll()

//    @Query(
//        """
//        SELECT vault_id, username, security_type, COUNT(*) AS occurrences
//        FROM items
//        GROUP BY security_type, username
//        ORDER BY occurrences DESC
//        LIMIT 20
//        """,
//    )
//    suspend fun getUsernamesFrequency(): List<UsernameFrequencyEntity>

    @Query("SELECT * FROM items WHERE vault_id == :vaultId AND deleted == 0")
    fun getVaultLogins(vaultId: String): List<ItemEntity>

    @Transaction
    suspend fun saveInTransaction(entities: List<ItemEntity>) {
        entities.chunked(500).forEach { chunk ->
            save(chunk)
        }
    }

    @Transaction
    suspend fun executeCloudMerge(cloudMerge: CloudMergeEntity) {
        cloudMerge.loginsToAdd.chunked(500).forEach { chunk ->
//            save(chunk)
        }

        cloudMerge.loginsToUpdate.chunked(500).forEach { chunk ->
//            save(chunk)
        }

        cloudMerge.loginsToTrash.chunked(500).forEach { chunk ->
//            save(chunk)
        }
    }

    @Query("SELECT MAX(updated_at) FROM logins")
    suspend fun getMostRecentUpdateTime(): Long?
}