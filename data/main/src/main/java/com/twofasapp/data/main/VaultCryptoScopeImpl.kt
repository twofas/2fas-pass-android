/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.core.common.crypto.AndroidKeyStore
import com.twofasapp.core.common.domain.Vault
import com.twofasapp.data.main.domain.VaultKeys

internal class VaultCryptoScopeImpl(
    private val androidKeyStore: AndroidKeyStore,
    private val vaultKeysRepository: VaultKeysRepository,
) : VaultCryptoScope {

    override suspend fun <T> withVaultCipher(
        vault: Vault,
        block: suspend VaultCipher.() -> T,
    ): T {
        return withVaultCipher(vault.id, block)
    }

    override suspend fun <T> withVaultCipher(
        vaultId: String,
        block: suspend VaultCipher.() -> T,
    ): T {
        return withVaultCipher(vaultKeysRepository.getVaultKeys(vaultId), block)
    }

    override suspend fun <T> withVaultCipher(vaultKeys: VaultKeys, block: suspend VaultCipher.() -> T): T {
        return block(VaultCipherImpl(androidKeyStore, vaultKeys))
    }

    override suspend fun getVaultCipher(vaultId: String): VaultCipher {
        return VaultCipherImpl(androidKeyStore, vaultKeysRepository.getVaultKeys(vaultId))
    }
}