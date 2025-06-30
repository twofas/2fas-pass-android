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
import com.twofasapp.data.main.local.model.VaultKeysEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultKeysDao {
    @Query("SELECT * FROM vault_keys")
    fun observe(): Flow<List<VaultKeysEntity>>

    @Query("SELECT * FROM vault_keys")
    suspend fun get(): List<VaultKeysEntity>

    @Query("SELECT * FROM vault_keys WHERE vault_id == :vaultId")
    suspend fun get(vaultId: String): VaultKeysEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: VaultKeysEntity)

    @Query("DELETE FROM vault_keys")
    suspend fun deleteAll()
}