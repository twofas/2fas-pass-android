/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.externalimport.navigation

import androidx.compose.runtime.Composable
import com.twofasapp.feature.externalimport.ui.externalimport.ExternalImportScreen

@Composable
fun ExternalImportRoute(
    openLogins: () -> Unit,
) {
    ExternalImportScreen(
        openLogins = openLogins,
    )
}