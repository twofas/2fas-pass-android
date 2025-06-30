/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.changepassword.set

internal data class SetNewPasswordUiState(
    val password: String = "",
    val passwordValid: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
)