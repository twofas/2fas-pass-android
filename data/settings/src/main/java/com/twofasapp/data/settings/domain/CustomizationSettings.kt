/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.settings.domain

import com.twofasapp.core.common.domain.SelectedTheme

data class CustomizationSettings(
    val selectedTheme: SelectedTheme = SelectedTheme.Auto,
    val dynamicColors: Boolean = false,
    val itemClickAction: ItemClickAction = ItemClickAction.View,
)