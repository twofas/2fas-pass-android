/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.connect.ui.commonmodal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonStyle
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.text.TextIcon

@Composable
internal fun SuccessState(
    title: String,
    subtitle: String,
    cta: String,
    onCta: () -> Unit,
) {
    Column {
        TextIcon(
            text = title,
            style = MdtTheme.typo.titleLarge,
            color = MdtTheme.color.onSurface,
            leadingIcon = MdtIcons.Check,
            leadingIconSize = 24.dp,
            leadingIconSpacer = 8.dp,
            leadingIconTint = MdtTheme.color.success,
        )

        Space(8.dp)

        Text(
            text = subtitle,
            style = MdtTheme.typo.bodyMedium,
            color = MdtTheme.color.onSurfaceVariant,
        )

        Space(20.dp)

        Button(
            text = cta,
            style = ButtonStyle.Tonal,
            onClick = { onCta() },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}