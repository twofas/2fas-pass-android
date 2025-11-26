/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform

import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.clearTextOrNull
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent

/**
 * Common UI state for all item forms.
 */
internal data class ItemFormUiState<T : ItemContent>(
    val initialised: Boolean = false,
    val initialItem: Item = Item.Empty,
    val item: Item = Item.Empty,
    val initialItemContent: T? = null,
    val itemContent: T? = null,
    val tags: List<Tag> = emptyList(),
) {
    val hasUnsavedChanges: Boolean
        get() = initialItem != item

    val valid: Boolean
        get() = when (itemContent) {
            is ItemContent.Login -> itemContent.name.isNotEmpty() && itemContent.notes.orEmpty().length <= 2048
            is ItemContent.SecureNote -> itemContent.name.isNotEmpty() && itemContent.text.clearTextOrNull.orEmpty().length <= ItemContent.SecureNote.Limit
            is ItemContent.PaymentCard -> false
            is ItemContent.Unknown -> false
        }
}