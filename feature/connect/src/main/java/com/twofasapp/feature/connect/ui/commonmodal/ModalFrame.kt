/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.connect.ui.commonmodal

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.browsers.Identicon
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.theme.RoundedShape16
import com.twofasapp.data.main.domain.Identicon

@Composable
internal fun ModalFrame(
    modifier: Modifier = Modifier,
    title: String?,
    subtitle: String?,
    identicon: Identicon?,
    onClose: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .background(MdtTheme.color.surfaceContainerLow)
            .fillMaxWidth()
            .animateContentSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconButton(
            icon = MdtIcons.Close,
            onClick = onClose,
            modifier = Modifier.align(Alignment.End),
        )

        identicon?.let {
            Identicon(
                modifier = Modifier,
                svgLight = identicon.svgLight,
                svgDark = identicon.svgDark,
            )
        }

        title?.let {
            Space(8.dp)

            Text(
                text = it,
                style = MdtTheme.typo.titleMedium,
                color = MdtTheme.color.onSurface,
            )
        }

        if (subtitle != null) {
            Space(4.dp)

            Text(
                text = subtitle,
                style = MdtTheme.typo.bodyMedium,
                color = MdtTheme.color.onSurfaceVariant,
            )
        }

        Space(20.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedShape16)
                .background(MdtTheme.color.surface)
                .padding(16.dp),
        ) {
            content()
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        ModalFrame(
            title = "Connecting with Browser Extension",
            subtitle = "Extension on Chrome (103)",
            identicon = Identicon.Empty,
        ) {
            Text(
                text = "This is test",
                style = MdtTheme.typo.titleMedium,
                color = MdtTheme.color.onSurface,
            )
        }
    }
}