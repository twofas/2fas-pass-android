/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.home

import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.Vault
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.ktx.filterBySearchQuery
import com.twofasapp.data.settings.domain.ItemClickAction
import com.twofasapp.data.settings.domain.SortingMethod

internal data class HomeUiState(
    val developerModeEnabled: Boolean = false,
    val vault: Vault = Vault.Empty,
    val items: List<Item> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val selectedTag: Tag? = null,
    val searchQuery: String = "",
    val searchFocused: Boolean = false,
    val editMode: Boolean = false,
    val scrollingUp: Boolean = false,
    val selectedItemIds: List<String> = emptyList(),
    val itemClickAction: ItemClickAction = ItemClickAction.View,
    val sortingMethod: SortingMethod = SortingMethod.NameAsc,
    val maxItems: Int = 0,
    val events: List<HomeUiEvent> = emptyList(),
) {
    val itemsFiltered: List<Item>
        get() = items
            .filter { it.content !is ItemContent.Unknown }
            .filter { item ->
                if (selectedTag == null) {
                    true
                } else {
                    item.tagIds.contains(selectedTag.id)
                }
            }
            .filterBySearchQuery(searchQuery)

    val isItemsLimitReached: Boolean
        get() = items.size >= maxItems

    val selectedItems: List<Item>
        get() = items.filter { selectedItemIds.contains(it.id) }
}

internal sealed interface HomeUiEvent {
    data object OpenQuickSetup : HomeUiEvent
    data class ShowToast(val message: String) : HomeUiEvent
}