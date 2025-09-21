/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.editItem

import com.twofasapp.core.common.domain.items.Item

internal data class EditItemUiState(
    val initialItem: Item? = null,
    val item: Item = Item.Empty,
    val isValid: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
)