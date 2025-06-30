/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.ui.picker

import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.common.ktx.filterBySearchQuery
import com.twofasapp.feature.autofill.service.parser.NodeStructure

internal data class AutofillPickerUiState(
    val nodeStructure: NodeStructure = NodeStructure.Empty,
    val searchQuery: String = "",
    val searchFocused: Boolean = false,
    val allLogins: List<Login> = emptyList(),
    val suggestedLogins: List<Login> = emptyList(),
    val otherLogins: List<Login> = emptyList(),
) {
    val suggestedLoginsFiltered: List<Login>
        get() = suggestedLogins.filterBySearchQuery(searchQuery)

    val otherLoginsFiltered: List<Login>
        get() = otherLogins.filterBySearchQuery(searchQuery)
}