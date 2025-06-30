/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.navigation

import androidx.compose.runtime.Composable
import com.twofasapp.feature.home.ui.editlogin.EditLoginScreen
import com.twofasapp.feature.home.ui.home.HomeScreen

@Composable
fun HomeRoute(
    openAddLogin: (String) -> Unit,
    openEditLogin: (String, String) -> Unit,
    openSettings: () -> Unit,
    openDeveloper: () -> Unit,
) {
    HomeScreen(
        openAddLogin = openAddLogin,
        openEditLogin = openEditLogin,
        openSettings = openSettings,
        openDeveloper = openDeveloper,
    )
}

@Composable
fun EditLoginRoute(
    close: () -> Unit,
) {
    EditLoginScreen(
        close = close,
    )
}