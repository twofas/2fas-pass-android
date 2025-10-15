/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.foundation.textfield

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.foundation.button.IconButton

fun VisualTransformation.Companion.SecretField(
    visible: Boolean,
    passwordColors: PasswordColors? = null,
): VisualTransformation {
    return if (visible) {
        if (passwordColors != null) {
            PasswordColorized(passwordColors)
        } else {
            None
        }
    } else {
        PasswordVisualTransformation()
    }
}

private class PasswordColorized(
    private val colors: PasswordColors,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            passwordColorized(text.toString(), colors),
            OffsetMapping.Identity,
        )
    }
}

@Composable
fun SecretFieldTrailingIcon(
    visible: Boolean,
    onToggle: () -> Unit = {},
) {
    IconButton(
        onClick = onToggle,
        icon = if (visible) {
            MdtIcons.VisibilityOff
        } else {
            MdtIcons.Visibility
        },
    )
}