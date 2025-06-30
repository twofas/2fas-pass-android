/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.core.common.domain.Vault
import com.twofasapp.data.main.domain.VaultKeys

interface VaultCryptoScope {
    companion object {
        val Empty = object : VaultCryptoScope {
            override suspend fun <T> withVaultCipher(vault: Vault, block: suspend VaultCipher.() -> T) = block(VaultCipherEmpty())
            override suspend fun <T> withVaultCipher(vaultId: String, block: suspend VaultCipher.() -> T) = block(VaultCipherEmpty())
            override suspend fun <T> withVaultCipher(vaultKeys: VaultKeys, block: suspend VaultCipher.() -> T) = block(VaultCipherEmpty())
            override suspend fun getVaultCipher(vaultId: String) = VaultCipherEmpty()
        }
    }

    suspend fun <T> withVaultCipher(vault: Vault, block: suspend VaultCipher.() -> T): T
    suspend fun <T> withVaultCipher(vaultId: String, block: suspend VaultCipher.() -> T): T
    suspend fun <T> withVaultCipher(vaultKeys: VaultKeys, block: suspend VaultCipher.() -> T): T
    suspend fun getVaultCipher(vaultId: String): VaultCipher
}