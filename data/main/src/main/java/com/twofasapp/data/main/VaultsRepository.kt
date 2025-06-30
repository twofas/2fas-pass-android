/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.core.common.domain.Vault
import kotlinx.coroutines.flow.Flow

interface VaultsRepository {
    fun observeVaults(): Flow<List<Vault>>
    suspend fun getVault(): Vault
    suspend fun getVault(id: String): Vault
    suspend fun createVault(vault: Vault)
    suspend fun deleteVault(vararg id: String)
    suspend fun deleteAll()
}