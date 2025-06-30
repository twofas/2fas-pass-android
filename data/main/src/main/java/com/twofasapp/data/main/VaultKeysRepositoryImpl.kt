/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.crypto.AndroidKeyStore
import com.twofasapp.core.common.crypto.encrypt
import com.twofasapp.core.common.ktx.decodeHex
import com.twofasapp.core.common.ktx.hmacSha256
import com.twofasapp.data.main.domain.VaultHashes
import com.twofasapp.data.main.domain.VaultKeys
import com.twofasapp.data.main.local.VaultKeysLocalSource
import com.twofasapp.data.main.local.VaultsLocalSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class VaultKeysRepositoryImpl(
    private val dispatchers: Dispatchers,
    private val androidKeyStore: AndroidKeyStore,
    private val localVaults: VaultsLocalSource,
    private val localVaultKeys: VaultKeysLocalSource,
) : VaultKeysRepository {

    override suspend fun generateVaultKeys(masterKeyHex: String, vaultId: String): VaultKeys {
        return withContext(dispatchers.io) {
            VaultKeys(
                vaultId = vaultId,
                trusted = encrypt(androidKeyStore.appKey, hmacSha256(masterKeyHex.decodeHex(), "$vaultId/tKey".toByteArray())),
                secret = encrypt(androidKeyStore.appKey, hmacSha256(masterKeyHex.decodeHex(), "$vaultId/sKey".toByteArray())),
                external = encrypt(androidKeyStore.appKey, hmacSha256(masterKeyHex.decodeHex(), "$vaultId/eKey".toByteArray())),
            )
        }
    }

    override suspend fun generateAndSaveVaultKeys(masterKeyHex: String) {
        withContext(dispatchers.io) {
            localVaults.get().forEach { vaultEntity ->
                localVaultKeys.save(
                    generateVaultKeys(masterKeyHex, vaultEntity.id),
                )
            }
        }
    }

    override suspend fun getVaultKeys(vaultId: String): VaultKeys {
        return withContext(dispatchers.io) {
            localVaultKeys.get(vaultId)
        }
    }

    override suspend fun getVaultKeys(): List<VaultKeys> {
        return withContext(dispatchers.io) {
            localVaultKeys.observeVaultKeys().first()
        }
    }

    override suspend fun clearInMemoryVaultKeys() {
        return withContext(dispatchers.io) {
            localVaultKeys.clearInMemoryVaultKeys()
        }
    }

    override suspend fun clearPersistedVaultKeys() {
        return withContext(dispatchers.io) {
            localVaultKeys.clearPersistedVaultKeys()
        }
    }

    override suspend fun generateVaultHashes(seedHex: String, vaultId: String): VaultHashes {
        return withContext(dispatchers.io) {
            VaultHashes(
                vaultId = vaultId,
                trusted = hmacSha256(seedHex.decodeHex(), "$vaultId/tKey".toByteArray()),
                secret = hmacSha256(seedHex.decodeHex(), "$vaultId/sKey".toByteArray()),
                external = hmacSha256(seedHex.decodeHex(), "$vaultId/eKey".toByteArray()),
            )
        }
    }

    override fun observeHasValidVaultKeys(): Flow<Boolean> {
        return localVaultKeys.observeVaultKeys().map { vaultKeys ->
            vaultKeys.isNotEmpty() && vaultKeys.all { it.valid }
        }
    }
}