/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.twofasapp.core.design.theme.ColorTokens
import com.twofasapp.core.design.theme.TypographyTokens

object MdtTheme {
    val color: ColorTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalColorTokens.current

    val typo: TypographyTokens
        @Composable
        @ReadOnlyComposable
        get() = TypographyTokens(color)
}