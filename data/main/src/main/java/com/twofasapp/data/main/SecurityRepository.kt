/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.domain.crypto.KdfSpec
import com.twofasapp.data.security.crypto.MasterKey
import com.twofasapp.data.security.crypto.Seed
import kotlinx.coroutines.flow.Flow

interface SecurityRepository {
    suspend fun generateSeed(entropy: ByteArray? = null): Seed
    suspend fun restoreSeed(words: List<String>): Seed
    suspend fun getSeed(): Seed
    suspend fun generateMasterKeyOnFirstLaunch(
        password: String,
        seed: Seed,
        kdfSpec: KdfSpec,
    ): MasterKey

    suspend fun generateMasterKey(
        password: String,
        seed: Seed,
        kdfSpec: KdfSpec,
    ): MasterKey

    suspend fun getMasterKeyWithPassword(password: String): String
    suspend fun saveMasterKeyEncryptedWithBiometrics(encryptedBytes: EncryptedBytes?)
    suspend fun saveMasterKeyEntropy(entropy: ByteArray)
    suspend fun saveMasterKeyKdfSpec(kdfSpec: KdfSpec)
    suspend fun saveBiometricsEnabled(enabled: Boolean)
    suspend fun saveEncryptionReference(masterKey: MasterKey)
    suspend fun getMasterKeyEntropy(): ByteArray?
    suspend fun getMasterKeyKdfSpec(): KdfSpec
    suspend fun checkCurrentPassword(password: String): Boolean
    fun observeBiometricsEnabled(): Flow<Boolean>
    fun observeMasterKeyEncryptedWithBiometrics(): Flow<EncryptedBytes?>
    suspend fun resetData()
}