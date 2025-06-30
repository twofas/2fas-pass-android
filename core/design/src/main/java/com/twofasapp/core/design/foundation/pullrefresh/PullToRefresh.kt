/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.foundation.pullrefresh

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PullToRefresh(
    modifier: Modifier = Modifier,
    refreshing: Boolean? = null,
    enabled: Boolean = true,
    onPullRefresh: () -> Unit = {},
    state: PullToRefreshState = rememberPullToRefreshState(),
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit,
) {
    val scope = rememberCoroutineScope()
    var localRefreshing by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pullToRefresh(
                enabled = enabled,
                state = state,
                isRefreshing = refreshing ?: localRefreshing,
                onRefresh = {
                    onPullRefresh()

                    // If the "refreshing" argument is not provided, use local mutable state
                    // to briefly animate the indicator. This prevents it from freezing on screen.
                    if (refreshing == null) {
                        localRefreshing = true
                        scope.launch {
                            delay(300)
                            localRefreshing = false
                        }
                    }
                },
            ),
        contentAlignment = contentAlignment,
    ) {
        content()

        PullToRefreshIndicator(
            state = state,
            isRefreshing = refreshing ?: localRefreshing,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}