/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.foundation.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.MdtTheme

@Composable
fun DropdownMenu(
    modifier: Modifier = Modifier,
    visible: Boolean,
    shape: Shape = RoundedCornerShape(12.dp),
    onDismissRequest: () -> Unit,
    anchor: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box {
        anchor()
        DropdownMenu(
            shape = shape,
            expanded = visible,
            onDismissRequest = onDismissRequest,
            modifier = modifier
                .widthIn(min = 160.dp)
                .background(MdtTheme.color.surfaceContainer),
            content = content,
        )
    }
}

@Composable
fun DropdownMenuItem(
    text: String? = null,
    textContent: (@Composable () -> Unit)? = null,
    testTag: String? = null,
    onClick: () -> Unit = {},
    leadingIcon: Painter? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    contentColor: Color = MdtTheme.color.onSurface,
    contentPadding: PaddingValues = PaddingValues(
        start = if (leadingIcon != null) 16.dp else 24.dp,
        end = 24.dp,
        top = 12.dp,
        bottom = 12.dp,
    ),
) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .clickable { onClick() }
            .padding(contentPadding),
    ) {
        if (leadingContent != null) {
            leadingContent()
        } else if (leadingIcon != null) {
            Icon(
                painter = leadingIcon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp),
            )

            Spacer(modifier = Modifier.width(16.dp))
        }

        if (textContent != null) {
            textContent()
        } else {
            Text(text = text.orEmpty(), color = contentColor)
        }
    }
}