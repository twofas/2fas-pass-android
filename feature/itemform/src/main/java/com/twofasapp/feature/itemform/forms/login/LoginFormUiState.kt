/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform.forms.login

import com.twofasapp.core.common.domain.PasswordGeneratorSettings
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent

internal data class LoginFormUiState(
    val initialised: Boolean = false,
    val initialItem: Item = Item.Empty,
    val initialItemContent: ItemContent.Login = ItemContent.Login.Empty,
    val item: Item = Item.Empty,
    val itemContent: ItemContent.Login = ItemContent.Login.Empty,
    val usernameSuggestions: List<String> = emptyList(),
    val passwordGeneratorSettings: PasswordGeneratorSettings = PasswordGeneratorSettings(),
    val tags: List<Tag> = emptyList(),
) {
    val valid: Boolean
        get() = itemContent.name.isNotEmpty() && itemContent.notes.orEmpty().length <= 2048

    val hasUnsavedChanges: Boolean
        get() = initialItem != item

    val usernameSuggestionsFiltered: List<String>
        get() = usernameSuggestions
            .filter { it.contains(itemContent.username.orEmpty().trim(), false) }
            .distinctBy { it.trim().lowercase() }
            .take(8)
}