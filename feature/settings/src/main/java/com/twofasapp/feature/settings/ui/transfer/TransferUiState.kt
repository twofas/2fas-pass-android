/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.transfer

internal data class TransferUiState(
    val maxItems: Int = 0,
    val isItemsLimitReached: Boolean = true,
)