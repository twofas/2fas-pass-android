/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.home

import com.twofasapp.core.common.domain.SecurityItem
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.Vault
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.common.ktx.filterBySearchQuery
import com.twofasapp.data.settings.domain.ItemClickAction
import com.twofasapp.data.settings.domain.SortingMethod
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class HomeUiState(
    val developerModeEnabled: Boolean = false,
    val vault: Vault = Vault.Empty,
    val items: List<Item> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val securityItems: ImmutableList<SecurityItem> = persistentListOf(),
    val selectedTag: Tag? = null,
    val selectedSecurityItem: SecurityItem? = null,
    val selectedItemType: ItemContentType? = null,
    val searchQuery: String = "",
    val searchFocused: Boolean = false,
    val editMode: Boolean = false,
    val scrollingUp: Boolean = false,
    val selectedItemIds: Set<String> = emptySet(),
    val itemClickAction: ItemClickAction = ItemClickAction.View,
    val sortingMethod: SortingMethod = SortingMethod.NameAsc,
    val maxItems: Int = 0,
    val events: List<HomeUiEvent> = emptyList(),
) {
    val itemsFiltered: List<Item>
        get() = items
            .filter { it.content !is ItemContent.Unknown }
            .filter { item ->
                if (selectedItemType == null) {
                    true
                } else {
                    item.contentType == selectedItemType
                }
            }
            .filter { item ->
                if (selectedTag == null) {
                    true
                } else {
                    item.tagIds.contains(selectedTag.id)
                }
            }
            .filter { item ->
                if (selectedSecurityItem == null) {
                    true
                } else {
                    item.securityType == selectedSecurityItem.type
                }
            }
            .filterBySearchQuery(searchQuery)

    val isItemsLimitReached: Boolean
        get() = items.size >= maxItems

    val selectedItems: List<Item>
        get() = items.filter { selectedItemIds.contains(it.id) }

    val allFilteredSelected =
        itemsFiltered.all { it.id in selectedItemIds }
}

internal sealed interface HomeUiEvent {
    data object OpenQuickSetup : HomeUiEvent
    data class ShowToast(val message: String) : HomeUiEvent
}