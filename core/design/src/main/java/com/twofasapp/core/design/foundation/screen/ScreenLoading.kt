/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.foundation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.twofasapp.core.design.foundation.progress.CircularProgress

@Composable
fun ScreenLoading(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        CircularProgress(
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    ScreenLoading(
        modifier = Modifier.fillMaxWidth(),
    )
}