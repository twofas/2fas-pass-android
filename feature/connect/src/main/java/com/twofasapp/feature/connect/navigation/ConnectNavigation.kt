/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.connect.navigation

import androidx.compose.runtime.Composable
import com.twofasapp.feature.connect.ui.connect.ConnectScreen

@Composable
fun ConnectRoute(
    onOpenHome: () -> Unit,
    onGoBack: () -> Unit,
) {
    ConnectScreen(
        onOpenHome = onOpenHome,
        onGoBack = onGoBack,
    )
}