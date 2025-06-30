/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.ui.app

import com.twofasapp.core.common.domain.AuthStatus
import com.twofasapp.core.common.domain.SelectedTheme

internal data class AppUiState(
    val selectedTheme: SelectedTheme = SelectedTheme.Auto,
    val dynamicColors: Boolean = false,
    val startDestination: AppStartDestination? = null,
    val showLock: Boolean? = null,
    val authStatus: AuthStatus? = null,
)

internal sealed interface AppStartDestination {
    data object Startup : AppStartDestination
    data object Main : AppStartDestination
}