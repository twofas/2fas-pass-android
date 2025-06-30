/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.externalimport.ui.externalimport

import com.twofasapp.feature.externalimport.import.ImportContent
import com.twofasapp.feature.externalimport.import.ImportSpec

internal data class ExternalImportUiState(
    val importSpec: ImportSpec = ImportSpec.Empty,
    val importState: ImportState = ImportState.Default,
    val loading: Boolean = false,
)

internal sealed interface ImportState {
    data object Default : ImportState
    data class ReadSuccess(val importContent: ImportContent) : ImportState
    data class Error(val msg: String?) : ImportState
}