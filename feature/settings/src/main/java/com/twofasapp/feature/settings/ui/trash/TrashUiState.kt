/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.trash

import com.twofasapp.core.common.domain.Login

internal data class TrashUiState(
    val maxItems: Int = 0,
    val loginsCount: Int = 0,
    val trashedLogins: List<Login> = emptyList(),
    val selected: List<String> = emptyList(),
) {
    val hasSelections: Boolean
        get() = selected.isNotEmpty()
}