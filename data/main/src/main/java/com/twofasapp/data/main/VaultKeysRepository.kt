/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.data.main.domain.VaultHashes
import com.twofasapp.data.main.domain.VaultKeys
import kotlinx.coroutines.flow.Flow

interface VaultKeysRepository {
    suspend fun generateVaultKeys(masterKeyHex: String, vaultId: String): VaultKeys
    suspend fun generateAndSaveVaultKeys(masterKeyHex: String)
    suspend fun getVaultKeys(vaultId: String): VaultKeys
    suspend fun getVaultKeys(): List<VaultKeys>
    suspend fun clearInMemoryVaultKeys()
    suspend fun clearPersistedVaultKeys()
    suspend fun generateVaultHashes(seedHex: String, vaultId: String): VaultHashes
    fun observeHasValidVaultKeys(): Flow<Boolean>
}