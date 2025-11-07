/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import android.net.Uri
import com.twofasapp.data.main.domain.VaultBackup
import com.twofasapp.data.main.domain.VaultKeys
import com.twofasapp.data.security.crypto.Seed

interface BackupRepository {
    suspend fun createVaultBackup(vaultId: String, includeDeleted: Boolean, decryptSecretFields: Boolean): VaultBackup
    suspend fun encryptVaultBackup(vaultBackup: VaultBackup): VaultBackup
    suspend fun serializeVaultBackup(vaultBackup: VaultBackup): String
    suspend fun readVaultBackup(fileUri: Uri): VaultBackup
    suspend fun readVaultBackup(content: String): VaultBackup
    suspend fun decryptVaultBackup(vaultBackup: VaultBackup, vaultKeys: VaultKeys, decryptSecretFields: Boolean): VaultBackup
    suspend fun decryptVaultBackup(vaultBackup: VaultBackup, masterKey: ByteArray, seed: Seed, decryptSecretFields: Boolean): VaultBackup
    suspend fun createSerializedVaultDataForBrowserExtension(
        version: Int,
        vaultId: String,
        deviceId: String,
        encryptionKey: ByteArray,
    ): ByteArray
}