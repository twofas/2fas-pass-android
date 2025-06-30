/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.foundation.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.preview.PreviewColumn

@Composable
fun LinearProgress(
    modifier: Modifier = Modifier,
    progress: Float,
    color: Color = MdtTheme.color.primary,
    brush: Brush? = null,
    trackColor: Color = MdtTheme.color.surfaceContainer,
    strokeCap: StrokeCap = StrokeCap.Round,
) {
    Box(
        modifier = modifier
            .then(
                when (strokeCap) {
                    StrokeCap.Round -> Modifier.clip(CircleShape)
                    StrokeCap.Butt -> Modifier
                    StrokeCap.Square -> Modifier
                    else -> Modifier
                },
            )
            .background(trackColor),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = progress)
                .then(
                    when (strokeCap) {
                        StrokeCap.Round -> Modifier.clip(CircleShape)
                        StrokeCap.Butt -> Modifier
                        StrokeCap.Square -> Modifier
                        else -> Modifier
                    },
                )
                .then(
                    if (brush != null) {
                        Modifier.background(brush = brush)
                    } else {
                        Modifier.background(color = color)
                    },
                ),
        )
    }
}

@Preview
@Composable
private fun Previews() {
    PreviewColumn {
        LinearProgress(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
            progress = 0.5f,
        )
    }
}