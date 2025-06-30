/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.restorevault.decyptvault

import com.twofasapp.data.main.domain.VaultBackup
import com.twofasapp.data.security.crypto.Seed
import com.twofasapp.feature.importvault.ui.ImportVaultState

internal data class DecryptVaultUiState(
    val screenState: ImportVaultState = ImportVaultState.ReadingFile,
    val encryptedBackup: VaultBackup = VaultBackup.Empty,
    val seed: Seed? = null,
    val masterKeyHex: String? = null,
    val passwordLoading: Boolean = false,
    val passwordError: String? = null,
    val seedError: String? = null,
    val words: List<String> = List(15) { "" },
)