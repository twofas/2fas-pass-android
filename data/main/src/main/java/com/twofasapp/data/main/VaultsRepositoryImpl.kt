/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.domain.Vault
import com.twofasapp.data.main.local.VaultsLocalSource
import com.twofasapp.data.main.mapper.VaultMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class VaultsRepositoryImpl(
    private val dispatchers: Dispatchers,
    private val vaultMapper: VaultMapper,
    private val localVaults: VaultsLocalSource,
) : VaultsRepository {

    override fun observeVaults(): Flow<List<Vault>> {
        return localVaults.observe().map { vaults ->
            vaults.map { vault -> vault.let(vaultMapper::mapToDomain) }
        }.flowOn(dispatchers.io)
    }

    override suspend fun getVault(): Vault {
        return withContext(dispatchers.io) {
            localVaults.get().first().let(vaultMapper::mapToDomain)
        }
    }

    override suspend fun getVault(id: String): Vault {
        return withContext(dispatchers.io) {
            localVaults.get(id).let(vaultMapper::mapToDomain)
        }
    }

    override suspend fun createVault(vault: Vault) {
        return withContext(dispatchers.io) {
            localVaults.save(vault.let(vaultMapper::mapToEntity))
        }
    }

    override suspend fun deleteVault(vararg id: String) {
        return withContext(dispatchers.io) {
            localVaults.delete(id.toList())
        }
    }

    override suspend fun deleteAll() {
        return withContext(dispatchers.io) {
            localVaults.deleteAll()
        }
    }
}