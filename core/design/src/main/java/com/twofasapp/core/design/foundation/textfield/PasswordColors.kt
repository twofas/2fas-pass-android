/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.foundation.textfield

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.twofasapp.core.design.MdtTheme

data class PasswordColors(
    val letters: Color,
    val digits: Color,
    val special: Color,
)

val passwordColors: PasswordColors
    @Composable
    get() = PasswordColors(
        letters = MdtTheme.color.onBackground,
        digits = Color(0xFF1FA85B),
        special = Color.Red,
    )

@Composable
fun passwordColorized(password: String): AnnotatedString {
    return passwordColorized(password, passwordColors)
}

fun passwordColorized(password: String, colors: PasswordColors): AnnotatedString {
    return buildAnnotatedString {
        password.forEach { letter ->
            if (letter.isDigit()) {
                withStyle(style = SpanStyle(color = colors.digits, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)) {
                    append(letter)
                }
            } else if (letter.isLetterOrDigit()) {
                withStyle(style = SpanStyle(color = colors.letters, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)) {
                    append(letter)
                }
            } else {
                withStyle(style = SpanStyle(color = colors.special, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)) {
                    append(letter)
                }
            }
        }
    }
}