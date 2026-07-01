/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonHeight
import com.twofasapp.core.design.foundation.button.ButtonStyle
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewColumn
import com.twofasapp.core.design.theme.RoundedShape16
import com.twofasapp.core.locale.MdtLocale

@Composable
internal fun AppReviewItem(
    modifier: Modifier = Modifier,
    onRateClick: () -> Unit = {},
    onDismissClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedShape16)
            .background(MdtTheme.color.surfaceContainer)
            .padding(all = 16.dp)
            .padding(top = 2.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MdtTheme.color.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = MdtIcons.StarShine,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MdtTheme.color.onPrimaryContainer,
                )
            }

            Space(16.dp)

            Column {
                Text(
                    text = MdtLocale.strings.homeAppReviewTitle,
                    style = MdtTheme.typo.titleMedium,
                    color = MdtTheme.color.onSurface,
                )

                Space(6.dp)

                Text(
                    text = MdtLocale.strings.homeAppReviewMsg,
                    style = MdtTheme.typo.bodyMedium,
                    color = MdtTheme.color.onSurfaceVariant,
                )
            }
        }

        Space(16.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            Button(
                text = MdtLocale.strings.homeAppReviewDismiss,
                style = ButtonStyle.Text,
                size = ButtonHeight.Small,
                onClick = onDismissClick,
            )

            Button(
                text = MdtLocale.strings.homeAppReviewRate,
                style = ButtonStyle.Filled,
                size = ButtonHeight.Small,
                leadingIcon = MdtIcons.Star,
                onClick = onRateClick,
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewColumn {
        AppReviewItem()
    }
}