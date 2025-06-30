/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.ui

import com.twofasapp.core.common.domain.AuthStatus
import com.twofasapp.core.common.domain.SelectedTheme

internal data class AutofillUiState(
    val selectedTheme: SelectedTheme = SelectedTheme.Auto,
    val dynamicColors: Boolean = false,
    val events: List<AutofillUiEvent> = emptyList(),
    val showLock: Boolean? = null,
    val authStatus: AuthStatus? = null,
)

internal sealed interface AutofillUiEvent