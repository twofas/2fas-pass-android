/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.foundation.progress

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.preview.PreviewColumn

enum class CircularProgressSize {
    Normal,
    Small,
    Tiny,
    Large,
}

@Composable
fun CircularProgress(
    modifier: Modifier = Modifier,
    size: CircularProgressSize = CircularProgressSize.Normal,
    color: Color = MdtTheme.color.primary,
    trackColor: Color = MdtTheme.color.transparent,
    strokeCap: StrokeCap = StrokeCap.Round,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size.size),
        color = color,
        strokeWidth = size.thickness,
        trackColor = trackColor,
        strokeCap = strokeCap,
    )
}

private val CircularProgressSize.size: Dp
    get() = when (this) {
        CircularProgressSize.Normal -> 32.dp
        CircularProgressSize.Small -> 24.dp
        CircularProgressSize.Tiny -> 20.dp
        CircularProgressSize.Large -> 40.dp
    }

private val CircularProgressSize.thickness: Dp
    get() = when (this) {
        CircularProgressSize.Normal -> 4.dp
        CircularProgressSize.Small -> 3.dp
        CircularProgressSize.Tiny -> 2.dp
        CircularProgressSize.Large -> 4.dp
    }

@Preview
@Composable
private fun Previews() {
    PreviewColumn {
        CircularProgressSize.entries.forEach { size ->
            CircularProgress(size = size)
        }
    }
}