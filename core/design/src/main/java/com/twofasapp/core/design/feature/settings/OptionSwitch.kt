/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.feature.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.checked.Switch
import com.twofasapp.core.design.foundation.preview.PreviewColumn
import com.twofasapp.core.design.foundation.preview.PreviewText
import com.twofasapp.core.design.foundation.preview.PreviewTextLong

@Composable
fun OptionSwitch(
    title: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = OptionEntryPadding,
    subtitle: String? = null,
    icon: Painter? = null,
    iconTint: Color? = null,
    titleColor: Color = MdtTheme.color.onSurface,
    descriptionColor: Color = MdtTheme.color.onSurfaceVariant,
    external: Boolean = false,
    enabled: Boolean = true,
    onToggle: ((Boolean) -> Unit)? = null,
) {
    OptionEntry(
        title = title,
        modifier = modifier,
        contentPadding = contentPadding,
        subtitle = subtitle,
        icon = icon,
        iconTint = iconTint,
        titleColor = titleColor,
        subtitleColor = descriptionColor,
        external = external,
        enabled = enabled,
        onClick = { onToggle?.invoke(checked.not()) },
        content = {
            Switch(checked = checked, onCheckedChange = {
                if (enabled) {
                    onToggle?.invoke(it)
                }
            })
        },
    )
}

@Preview
@Composable
private fun Previews() {
    PreviewColumn {
        OptionSwitch(
            title = PreviewText,
            checked = true,
            subtitle = PreviewTextLong,
            icon = MdtIcons.Placeholder,
        )
    }
}