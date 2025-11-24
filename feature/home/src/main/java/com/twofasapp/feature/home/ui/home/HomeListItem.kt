/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.home

import com.twofasapp.core.design.foundation.lazy.ListItem

internal sealed class HomeListItem(key: Any? = null, type: Any? = null) : ListItem(key, type) {
    data object SearchBar : HomeListItem()
    data object Empty : HomeListItem()
    data object Footer : HomeListItem()
    data class HomeItem(private val id: String) : HomeListItem("HomeItem:$id", "HomeItem")
    data class HomeItemsRow(private val index: Int, private val ids: List<String>) : HomeListItem("HomeItemsRow:$index:${ids.joinToString()}", "HomeItemsRow")
}