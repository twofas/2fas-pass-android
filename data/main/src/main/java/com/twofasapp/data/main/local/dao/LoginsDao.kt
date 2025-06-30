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
import com.twofasapp.data.main.local.model.LoginEntity
import com.twofasapp.data.main.local.model.UsernameFrequencyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoginsDao {
    @Query("SELECT * FROM logins WHERE vault_id == :vaultId AND deleted == 0")
    fun observe(vaultId: String): Flow<List<LoginEntity>>

    @Query("SELECT * FROM logins WhERE deleted == 1")
    fun observeDeleted(): Flow<List<LoginEntity>>

    @Query("SELECT * FROM logins WHERE id == :id")
    suspend fun get(id: String): LoginEntity

    @Query("SELECT * FROM logins WHERE deleted == 0")
    suspend fun get(): List<LoginEntity>

    @Query("SELECT COUNT(id) FROM logins WHERE deleted == 0")
    suspend fun count(): Int

    @Query("SELECT * FROM logins")
    suspend fun getWithDeleted(): List<LoginEntity>

    @Query("SELECT * FROM logins WHERE id IN (:ids) AND deleted == 0")
    suspend fun get(ids: List<String>): List<LoginEntity>

    @Query("SELECT * FROM logins WHERE id IN (:ids) AND deleted == 1")
    suspend fun getDeleted(ids: List<String>): List<LoginEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: LoginEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entities: List<LoginEntity>)

    @Query("DELETE FROM logins WHERE id IN (:ids)")
    suspend fun delete(ids: List<String>)

    @Query("DELETE FROM logins")
    suspend fun deleteAll()

    @Query(
        """
        SELECT vault_id, username, security_type, COUNT(*) AS occurrences 
        FROM logins
        GROUP BY security_type, username
        ORDER BY occurrences DESC
        LIMIT 20
        """,
    )
    suspend fun getUsernamesFrequency(): List<UsernameFrequencyEntity>

    @Query("SELECT * FROM logins WHERE vault_id == :vaultId AND deleted == 0")
    fun getVaultLogins(vaultId: String): List<LoginEntity>

    @Transaction
    suspend fun saveInTransaction(entities: List<LoginEntity>) {
        entities.chunked(500).forEach { chunk ->
            save(chunk)
        }
    }

    @Transaction
    suspend fun executeCloudMerge(cloudMerge: CloudMergeEntity) {
        cloudMerge.loginsToAdd.chunked(500).forEach { chunk ->
            save(chunk)
        }

        cloudMerge.loginsToUpdate.chunked(500).forEach { chunk ->
            save(chunk)
        }

        cloudMerge.loginsToTrash.chunked(500).forEach { chunk ->
            save(chunk)
        }
    }

    @Query("SELECT MAX(updated_at) FROM logins")
    suspend fun getMostRecentUpdateTime(): Long?
}