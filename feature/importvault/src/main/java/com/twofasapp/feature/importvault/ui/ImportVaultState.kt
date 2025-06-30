/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.importvault.ui

sealed interface ImportVaultState {
    data object ReadingFile : ImportVaultState
    data class ReadingFileError(val title: String, val msg: String) : ImportVaultState
    data object Default : ImportVaultState
    data object ScanDecryptionKit : ImportVaultState
    data object EnterSeed : ImportVaultState
    data object EnterMasterPassword : ImportVaultState
    data object ImportingFile : ImportVaultState
    data class ImportingFileError(val title: String, val msg: String) : ImportVaultState
    data object ImportingFileSuccess : ImportVaultState
}