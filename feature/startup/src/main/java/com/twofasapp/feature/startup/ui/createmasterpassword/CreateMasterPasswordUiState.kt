/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.createmasterpassword

internal data class CreateMasterPasswordUiState(
    val password: String = "",
    val passwordValid: Boolean = false,
    val loading: Boolean = false,
    val masterKeyHashHex: String? = null,
    val events: List<CreateMasterPasswordUiEvent> = emptyList(),
)

internal sealed interface CreateMasterPasswordUiEvent {
    data object Complete : CreateMasterPasswordUiEvent
}