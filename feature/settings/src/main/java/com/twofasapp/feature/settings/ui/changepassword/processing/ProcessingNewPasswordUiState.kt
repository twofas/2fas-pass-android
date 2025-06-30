/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.changepassword.processing

internal data class ProcessingNewPasswordUiState(
    val step: Step? = null,
    val processingMessage: String? = null,
    val error: String? = null,
    val newMasterKeyHex: String? = null,
) {
    sealed interface Step {
        data object Processing : Step
        data object Success : Step
        data class Error(val message: String) : Step
    }
}