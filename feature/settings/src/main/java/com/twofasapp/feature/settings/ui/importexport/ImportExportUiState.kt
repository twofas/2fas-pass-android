/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.importexport

import com.twofasapp.core.common.domain.crypto.EncryptionSpec
import com.twofasapp.data.main.domain.VaultBackup

internal data class ImportExportUiState(
    val vaultBackupToImport: VaultBackup = VaultBackup.Empty,
    val importLoading: Boolean = false,
    val events: List<ImportExportUiEvent> = emptyList(),
    val maxItems: Int = 0,
    val isItemsLimitReached: Boolean = true,
)

internal sealed interface ImportExportUiEvent {
    data object ShowInvalidSchemaError : ImportExportUiEvent
    data class ShowDecryptionDialog(
        val encryptionSpec: EncryptionSpec,
    ) : ImportExportUiEvent

    data object ShowErrorDialog : ImportExportUiEvent
    data object ResetExportModal : ImportExportUiEvent
    data object ImportSuccess : ImportExportUiEvent
}