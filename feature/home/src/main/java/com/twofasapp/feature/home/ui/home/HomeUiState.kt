/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.home

import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.common.domain.Vault
import com.twofasapp.core.common.ktx.filterBySearchQuery
import com.twofasapp.data.settings.domain.LoginClickAction
import com.twofasapp.data.settings.domain.SortingMethod

internal data class HomeUiState(
    val developerModeEnabled: Boolean = false,
    val vault: Vault = Vault.Empty,
    val logins: List<Login> = emptyList(),
    val searchQuery: String = "",
    val searchFocused: Boolean = false,
    val loginClickAction: LoginClickAction = LoginClickAction.View,
    val sortingMethod: SortingMethod = SortingMethod.NameAsc,
    val maxItems: Int = 0,
    val events: List<HomeUiEvent> = emptyList(),
) {
    val loginsFiltered: List<Login>
        get() = logins.filterBySearchQuery(searchQuery)

    val isItemsLimitReached: Boolean
        get() = logins.size >= maxItems
}

internal sealed interface HomeUiEvent {
    data object OpenQuickSetup : HomeUiEvent
    data class CopyPasswordToClipboard(val text: String) : HomeUiEvent
}