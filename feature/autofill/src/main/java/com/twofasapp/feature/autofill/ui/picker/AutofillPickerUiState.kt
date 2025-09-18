/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.ui.picker

import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.ktx.filterBySearchQuery
import com.twofasapp.feature.autofill.service.parser.NodeStructure

internal data class AutofillPickerUiState(
    val nodeStructure: NodeStructure = NodeStructure.Empty,
    val searchQuery: String = "",
    val searchFocused: Boolean = false,
    val suggestedItems: List<Item> = emptyList(),
    val otherItems: List<Item> = emptyList(),
) {
    val suggestedItemsFiltered: List<Item>
        get() = suggestedItems
            .filter { it.contentType.fillable }
            .filterBySearchQuery(searchQuery)

    val otherItemsFiltered: List<Item>
        get() = otherItems
            .filter { it.contentType.fillable }
            .filterBySearchQuery(searchQuery)
}