/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.lock.ui.composables

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.textfield.Password
import com.twofasapp.core.design.foundation.textfield.PasswordTrailingIcon
import com.twofasapp.core.design.foundation.textfield.TextField
import com.twofasapp.core.locale.MdtLocale

@Composable
fun MasterPasswordField(
    modifier: Modifier = Modifier,
    password: String,
    error: String? = null,
    enabled: Boolean = true,
    onPasswordChange: (String) -> Unit = {},
    onDone: () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }

    TextField(
        value = password,
        onValueChange = onPasswordChange,
        labelText = MdtLocale.strings.masterPasswordLabel,
        supportingText = error,
        isError = error != null,
        modifier = modifier,
        singleLine = true,
        maxLines = 1,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        visualTransformation = VisualTransformation.Password(passwordVisible),
        trailingIcon = {
            PasswordTrailingIcon(
                passwordVisible = passwordVisible,
                onToggle = { passwordVisible = passwordVisible.not() },
            )
        },
        keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
            if (enabled) {
                onDone()
            }
        }),
    )
}

@Preview
@Composable
private fun Previews() {
    PreviewTheme {
        MasterPasswordField(
            password = "123",
        )
    }
}