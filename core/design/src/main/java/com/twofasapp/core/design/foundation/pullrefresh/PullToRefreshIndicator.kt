/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.foundation.pullrefresh

import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.PositionalThreshold
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.twofasapp.core.design.MdtTheme

@Composable
fun PullToRefreshIndicator(
    modifier: Modifier = Modifier,
    state: PullToRefreshState,
    isRefreshing: Boolean,
    containerColor: Color = MdtTheme.color.surfaceContainer,
    color: Color = MdtTheme.color.primary,
    threshold: Dp = PositionalThreshold,
) {
    Indicator(
        state = state,
        isRefreshing = isRefreshing,
        modifier = modifier,
        containerColor = containerColor,
        color = color,
        threshold = threshold,
    )
}