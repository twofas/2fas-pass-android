/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.feature.browsers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.LocalDarkMode
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.image.AsyncImage
import com.twofasapp.core.design.foundation.preview.PreviewAllThemesInColumn
import com.twofasapp.core.design.theme.RoundedShape16

@Composable
fun Identicon(
    modifier: Modifier = Modifier,
    svgLight: String,
    svgDark: String,
    size: Dp = 64.dp,
    contentPadding: Dp = 10.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedShape16)
            .background(MdtTheme.color.surface)
            .border(1.dp, MdtTheme.color.outline.copy(alpha = 0.16f), RoundedShape16)
            .padding(contentPadding),
    ) {
        AsyncImage(
            svgIcon = if (LocalDarkMode.current) svgDark else svgLight,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewAllThemesInColumn {
        Identicon(
            svgLight = "",
            svgDark = "",
        )
    }
}