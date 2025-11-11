/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.navigation

import androidx.compose.runtime.Composable
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.feature.home.ui.editItem.EditItemScreen
import com.twofasapp.feature.home.ui.home.HomeScreen

@Composable
fun HomeRoute(
    openAddItem: (vaultId: String, itemContentType: ItemContentType) -> Unit,
    openEditItem: (itemId: String, vaultId: String, itemContentType: ItemContentType) -> Unit,
    openManageTags: () -> Unit,
    openQuickSetup: () -> Unit,
    openDeveloper: () -> Unit,
) {
    HomeScreen(
        openAddItem = openAddItem,
        openEditItem = openEditItem,
        openManageTags = openManageTags,
        openQuickSetup = openQuickSetup,
        openDeveloper = openDeveloper,
    )
}

@Composable
fun EditItemRoute(
    close: () -> Unit,
) {
    EditItemScreen(
        close = close,
    )
}