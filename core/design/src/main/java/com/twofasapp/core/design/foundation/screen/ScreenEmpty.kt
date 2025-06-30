/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.foundation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.preview.PreviewTheme

@Composable
fun ScreenEmpty(
    modifier: Modifier = Modifier,
    text: String,
    icon: Painter,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = MdtTheme.color.onSurfaceVariant,
            modifier = Modifier.size(48.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = text,
            color = MdtTheme.color.onSurfaceVariant,
            style = MdtTheme.typo.regular.base,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        ScreenEmpty(
            modifier = Modifier.fillMaxSize(),
            icon = MdtIcons.Info,
            text = "Empty state",
        )
    }
}