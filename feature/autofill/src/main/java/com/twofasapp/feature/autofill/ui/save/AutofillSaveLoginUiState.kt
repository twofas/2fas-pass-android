/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.ui.save

import com.twofasapp.core.common.domain.items.Item

internal data class AutofillSaveLoginUiState(
    val initialItem: Item? = null,
    val item: Item = Item.Empty,
    val isValid: Boolean = false,
)