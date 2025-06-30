/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.core.common.build.Device
import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.crypto.RandomGenerator
import com.twofasapp.core.common.crypto.decrypt
import com.twofasapp.core.common.crypto.encrypt
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.domain.crypto.KdfSpec
import com.twofasapp.core.common.domain.crypto.asDomain
import com.twofasapp.core.common.domain.crypto.asEntity
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.core.common.ktx.decodeHex
import com.twofasapp.core.common.ktx.encodeBase64
import com.twofasapp.core.common.storage.DataStoreOwner
import com.twofasapp.data.main.local.SecurityLocalSource
import com.twofasapp.data.security.crypto.MasterKey
import com.twofasapp.data.security.crypto.MasterKeyGenerator
import com.twofasapp.data.security.crypto.Seed
import com.twofasapp.data.security.crypto.SeedGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class SecurityRepositoryImpl(
    dataStoreOwner: DataStoreOwner,
    private val dispatchers: Dispatchers,
    private val local: SecurityLocalSource,
    private val device: Device,
    private val seedGenerator: SeedGenerator,
    private val masterKeyGenerator: MasterKeyGenerator,
) : SecurityRepository, DataStoreOwner by dataStoreOwner {

    override suspend fun generateSeed(entropy: ByteArray?): Seed {
        return withContext(dispatchers.io) {
            seedGenerator.generate(entropy ?: RandomGenerator.generate(20))
        }
    }

    override suspend fun restoreSeed(words: List<String>): Seed {
        return withContext(dispatchers.io) {
            seedGenerator.restore(words)
        }
    }

    override suspend fun getSeed(): Seed {
        return generateSeed(getMasterKeyEntropy())
    }

    override suspend fun generateMasterKeyOnFirstLaunch(
        password: String,
        seed: Seed,
        kdfSpec: KdfSpec,
    ): MasterKey {
        return withContext(dispatchers.io) {
            generateMasterKey(password, seed, kdfSpec)
                .also { this@SecurityRepositoryImpl.saveEncryptionReference(it) }
        }
    }

    override suspend fun generateMasterKey(password: String, seed: Seed, kdfSpec: KdfSpec): MasterKey {
        return withContext(dispatchers.io) {
            masterKeyGenerator.generate(
                password = password,
                seedHex = seed.seedHex,
                saltHex = seed.saltHex,
                kdfSpec = kdfSpec,
            )
        }
    }

    override suspend fun resetData() {
        withContext(dispatchers.io) {
            local.masterKeyEntropy.delete()
            local.masterKeyKdfSpec.delete()
            local.encryptionReference.delete()
            local.masterKeyBiometricsEncrypted.delete()
        }
    }

    override suspend fun saveMasterKeyEncryptedWithBiometrics(encryptedBytes: EncryptedBytes?) {
        withContext(dispatchers.io) { local.masterKeyBiometricsEncrypted.set(encryptedBytes?.encodeBase64()) }
    }

    override suspend fun saveMasterKeyEntropy(entropy: ByteArray) {
        withContext(dispatchers.io) { local.masterKeyEntropy.set(entropy.encodeBase64()) }
    }

    override suspend fun saveMasterKeyKdfSpec(kdfSpec: KdfSpec) {
        withContext(dispatchers.io) { local.masterKeyKdfSpec.set(kdfSpec.asEntity()) }
    }

    override suspend fun saveBiometricsEnabled(enabled: Boolean) {
        withContext(dispatchers.io) {
            local.biometricsEnabled.set(enabled)

            if (enabled.not()) {
                local.masterKeyBiometricsEncrypted.delete()
            }
        }
    }

    override suspend fun saveEncryptionReference(masterKey: MasterKey) {
        local.encryptionReference.set(
            encrypt(
                key = masterKey.hashHex.decodeHex(),
                data = device.uniqueId(),
            ).encodeBase64(),
        )
    }

    override suspend fun getMasterKeyEntropy(): ByteArray? {
        return withContext(dispatchers.io) { local.masterKeyEntropy.get()?.decodeBase64() }
    }

    override suspend fun getMasterKeyKdfSpec(): KdfSpec {
        return withContext(dispatchers.io) { local.masterKeyKdfSpec.get()?.asDomain() ?: KdfSpec.Argon2id() }
    }

    override suspend fun checkCurrentPassword(password: String): Boolean {
        return withContext(dispatchers.io) {
            return@withContext try {
                getMasterKeyWithPassword(password = password)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    override suspend fun getMasterKeyWithPassword(password: String): String {
        return withContext(dispatchers.io) {
            val seed = generateSeed(getMasterKeyEntropy())
            val masterKey = masterKeyGenerator.generate(
                password = password,
                seedHex = seed.seedHex,
                saltHex = seed.saltHex,
                kdfSpec = local.masterKeyKdfSpec.get()?.asDomain() ?: KdfSpec.Argon2id(),
            )

            tryDecryptEncryptionReference(masterKey)

            masterKey.hashHex
        }
    }

    override fun observeBiometricsEnabled(): Flow<Boolean> {
        return local.biometricsEnabled.asFlow()
    }

    override fun observeMasterKeyEncryptedWithBiometrics(): Flow<EncryptedBytes?> {
        return local.masterKeyBiometricsEncrypted.asFlow().map { it?.let { EncryptedBytes(it.decodeBase64()) } }
    }

    private suspend fun tryDecryptEncryptionReference(key: MasterKey) {
        decrypt(
            key = key.hashHex.decodeHex(),
            data = local.encryptionReference.get()?.decodeBase64() ?: byteArrayOf(),
        )
    }
}