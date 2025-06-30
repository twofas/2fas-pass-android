/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.foundation.tooltip

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.AppTheme
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.preview.PreviewColumn
import com.twofasapp.core.design.foundation.preview.PreviewTextMedium
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.progress.Timer
import com.twofasapp.core.design.foundation.text.TextIcon
import com.twofasapp.core.design.theme.RoundedShape8

@Composable
fun Tooltip(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    visible: Boolean,
    text: String,
    icon: Painter? = null,
) {
    if (visible) {
        Timer(
            duration = 3000,
            onElapsed = { onDismissRequest() },
        )
    }

    DisposableEffect(Unit) {
        onDispose { onDismissRequest() }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = spring(
                stiffness = Spring.StiffnessMedium,
                visibilityThreshold = IntOffset.VisibilityThreshold,
            ),
            initialOffsetY = { it / 2 },
        ),
        exit = slideOutVertically(
            animationSpec = spring(
                stiffness = Spring.StiffnessMedium,
                visibilityThreshold = IntOffset.VisibilityThreshold,
            ),
            targetOffsetY = { it },
        ),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .shadow(12.dp, RoundedShape8)
                .padding(4.dp)
                .fillMaxWidth()
                .clip(RoundedShape8)
                .background(MdtTheme.color.surfaceContainer)
                .padding(horizontal = 12.dp, vertical = if (icon == null) 16.dp else 12.dp),
        ) {
            TextIcon(
                text = text,
                style = MdtTheme.typo.medium.sm,
                color = MdtTheme.color.secondary,
                leadingIcon = icon,
                leadingIconTint = MdtTheme.color.secondary,
                leadingIconSpacer = 10.dp,
                leadingIconSize = 20.dp,
            )
        }
    }
}

@Preview
@Composable
private fun Previews() {
    PreviewColumn {
        PreviewTheme {
            Tooltip(
                modifier = Modifier,
                onDismissRequest = {},
                visible = true,
                text = PreviewTextMedium,
            )

            Tooltip(
                modifier = Modifier,
                onDismissRequest = {},
                visible = true,
                text = PreviewTextMedium,
                icon = MdtIcons.Placeholder,
            )
        }

        PreviewTheme(appTheme = AppTheme.Light) {
            Tooltip(
                modifier = Modifier,
                onDismissRequest = {},
                visible = true,
                text = PreviewTextMedium,
            )
        }
    }
}