/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui

import com.twofasapp.core.common.crypto.Uuid
import com.twofasapp.core.common.domain.Vault
import com.twofasapp.core.common.domain.crypto.KdfSpec
import com.twofasapp.core.common.ktx.decodeHex
import com.twofasapp.core.common.time.TimeProvider
import com.twofasapp.data.main.SecurityRepository
import com.twofasapp.data.main.VaultKeysRepository
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.security.crypto.MasterKey
import com.twofasapp.data.security.crypto.Seed

internal class StartupConfig(
    private val securityRepository: SecurityRepository,
    private val vaultsRepository: VaultsRepository,
    private val vaultKeysRepository: VaultKeysRepository,
    private val timeProvider: TimeProvider,
) {
    var seed: Seed? = null
    var masterKey: MasterKey? = null

    suspend fun finishStartup(
        vaultId: String = Uuid.generate(),
        vaultName: String? = null,
        vaultCreatedAt: Long? = null,
        vaultUpdatedAt: Long? = null,
    ) {
        val now = timeProvider.currentTimeUtc()

        vaultsRepository.createVault(
            Vault(
                id = vaultId,
                name = vaultName ?: "Main Vault",
                createdAt = vaultCreatedAt ?: now,
                updatedAt = vaultUpdatedAt ?: now,
            ),
        )

        vaultKeysRepository.generateAndSaveVaultKeys(masterKey!!.hashHex)

        securityRepository.saveMasterKeyEntropy(entropy = seed!!.entropyHex.decodeHex())
        securityRepository.saveMasterKeyKdfSpec(KdfSpec.Argon2id())
        clear()
    }

    fun clear() {
        seed = null
        masterKey = null
    }

    suspend fun clearStorage() {
        vaultsRepository.deleteAll()
        vaultKeysRepository.clearInMemoryVaultKeys()
        vaultKeysRepository.clearPersistedVaultKeys()
        securityRepository.resetData()
    }
}