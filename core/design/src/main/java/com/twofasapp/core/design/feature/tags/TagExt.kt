/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.feature.tags

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.TagColor
import com.twofasapp.core.design.MdtTheme

@Composable
fun Tag.iconTint(): Color {
    return color?.iconTint() ?: MdtTheme.color.tagGray
}

@Composable
fun TagColor.iconTint(): Color {
    return when (this) {
        TagColor.Cyan -> MdtTheme.color.tagCyan
        TagColor.Gray -> MdtTheme.color.tagGray
        TagColor.Green -> MdtTheme.color.tagGreen
        TagColor.Indigo -> MdtTheme.color.tagIndigo
        TagColor.Orange -> MdtTheme.color.tagOrange
        TagColor.Purple -> MdtTheme.color.tagPurple
        TagColor.Red -> MdtTheme.color.tagRed
        is TagColor.Unknown -> MdtTheme.color.tagGray
        TagColor.Yellow -> MdtTheme.color.tagYellow
    }
}