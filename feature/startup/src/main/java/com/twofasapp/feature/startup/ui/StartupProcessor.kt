/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui

import android.os.SystemClock
import com.twofasapp.core.common.crypto.Uuid
import com.twofasapp.core.common.domain.Vault
import com.twofasapp.core.common.domain.crypto.KdfSpec
import com.twofasapp.core.common.ktx.decodeHex
import com.twofasapp.core.common.storage.DataStoreOwner
import com.twofasapp.core.common.storage.serializedPrefNullable
import com.twofasapp.core.common.time.TimeProvider
import com.twofasapp.data.main.SecurityRepository
import com.twofasapp.data.main.VaultKeysRepository
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.security.crypto.MasterKey
import com.twofasapp.data.security.crypto.Seed
import java.time.Duration

internal class StartupProcessor(
    dataStoreOwner: DataStoreOwner,
    private val securityRepository: SecurityRepository,
    private val vaultsRepository: VaultsRepository,
    private val vaultKeysRepository: VaultKeysRepository,
    private val timeProvider: TimeProvider,
) : DataStoreOwner by dataStoreOwner {

    private val startupData by serializedPrefNullable(
        serializer = StartupData.serializer(),
        name = "startupData",
        encrypted = true,
    )

    suspend fun setSeed(seed: Seed) {
        updateStartupData { data ->
            data.copy(
                timestamp = SystemClock.elapsedRealtime(),
                words = seed.words,
                entropyHex = seed.entropyHex,
                seedHex = seed.seedHex,
                saltHex = seed.saltHex,
            )
        }
    }

    suspend fun getSeed(): Seed {
        val data = startupData.get()!!

        return Seed(
            words = data.words,
            entropyHex = data.entropyHex,
            seedHex = data.seedHex,
            saltHex = data.saltHex,
        )
    }

    suspend fun setMasterKey(masterKey: MasterKey) {
        updateStartupData { data ->
            data.copy(
                timestamp = SystemClock.elapsedRealtime(),
                masterKeyHashHex = masterKey.hashHex,
            )
        }
    }

    suspend fun getMasterKey(): MasterKey {
        return MasterKey(
            hashHex = startupData.get()!!.masterKeyHashHex,
        )
    }

    private suspend fun updateStartupData(action: (StartupData) -> StartupData) {
        startupData.set(
            startupData.get()?.let(action) ?: action(StartupData.Empty),
        )
    }

    suspend fun isStartupDataExpired(): Boolean {
        val data = startupData.get() ?: return false

        if (data.timestamp == 0L) {
            return false
        }

        return SystemClock.elapsedRealtime() - data.timestamp > Duration.ofHours(1).toMillis() // Consider data valid for 1 hour
    }

    suspend fun finish(
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

        val data = startupData.get()!!

        vaultKeysRepository.generateAndSaveVaultKeys(data.masterKeyHashHex)

        securityRepository.saveMasterKeyEntropy(entropy = data.entropyHex.decodeHex())
        securityRepository.saveMasterKeyKdfSpec(KdfSpec.Argon2id())
        clearStartupData()
    }

    suspend fun clearStartupData() {
        startupData.delete()
    }

    suspend fun clearVaultsData() {
        vaultsRepository.deleteAll()
        vaultKeysRepository.clearInMemoryVaultKeys()
        vaultKeysRepository.clearPersistedVaultKeys()
        securityRepository.resetData()
    }
}