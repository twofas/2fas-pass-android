/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.local

import com.twofasapp.data.main.domain.VaultKeys
import com.twofasapp.data.main.local.dao.VaultKeysDao
import com.twofasapp.data.main.local.model.VaultKeysEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import timber.log.Timber

internal class VaultKeysLocalSource(
    private val dao: VaultKeysDao,
) {
    private val inMemory = MutableStateFlow<Map<String, VaultKeys>>(emptyMap())

    suspend fun save(vaultKeys: VaultKeys) {
        inMemory.update { it.plus(vaultKeys.vaultId to vaultKeys) }

        dao.save(
            VaultKeysEntity(
                vaultId = vaultKeys.vaultId,
                trusted = vaultKeys.trusted,
            ),
        )
    }

    suspend fun get(vaultId: String): VaultKeys {
        return VaultKeys(
            vaultId = vaultId,
            trusted = inMemory.value[vaultId]?.trusted ?: dao.get(vaultId)?.trusted,
            secret = inMemory.value[vaultId]?.secret,
            external = inMemory.value[vaultId]?.external,
        )
    }

    fun observeVaultKeys(): Flow<List<VaultKeys>> {
        return combine(
            dao.observe(),
            inMemory,
        ) { a, b -> Pair(a, b) }.map { (entities, vaultKeysMap) ->
            entities.map { entity ->
                VaultKeys(
                    vaultId = entity.vaultId,
                    trusted = vaultKeysMap[entity.vaultId]?.trusted ?: entity.trusted,
                    secret = vaultKeysMap[entity.vaultId]?.secret,
                    external = vaultKeysMap[entity.vaultId]?.external,
                )
            }
        }
    }

    suspend fun clearInMemoryVaultKeys() {
        Timber.tag("VaultKeys").i("clearInMemoryVaultKeys")
        inMemory.emit(emptyMap())
    }

    suspend fun clearPersistedVaultKeys() {
        Timber.tag("VaultKeys").i("clearPersistedVaultKeys")
        dao.deleteAll()
    }
}