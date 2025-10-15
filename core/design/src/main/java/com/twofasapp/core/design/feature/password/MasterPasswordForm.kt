/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.feature.password

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.preview.PreviewText
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.text.TextIcon
import com.twofasapp.core.design.foundation.textfield.SecretField
import com.twofasapp.core.design.foundation.textfield.SecretFieldTrailingIcon
import com.twofasapp.core.design.foundation.textfield.TextField
import com.twofasapp.core.locale.MdtLocale

@Composable
fun MasterPasswordForm(
    modifier: Modifier = Modifier,
    password: String,
    onPasswordChange: (String) -> Unit = {},
    onPasswordValidationChange: (Boolean) -> Unit = {},
    onDone: () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    var passwordConfirm by remember { mutableStateOf(password) }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordConfirmVisible by remember { mutableStateOf(false) }
    var valid by remember { mutableStateOf(false) }

    var lengthValid by remember { mutableStateOf(false) }
    var passwordsMatch by remember { mutableStateOf(false) }

    LaunchedEffect(password) {
        lengthValid = MasterPasswordValidator.minLength(password)
        passwordsMatch = MasterPasswordValidator.passwordsMatch(password, passwordConfirm)
        valid = MasterPasswordValidator.valid(password, passwordConfirm)
    }

    LaunchedEffect(passwordConfirm) {
        passwordsMatch = MasterPasswordValidator.passwordsMatch(password, passwordConfirm)
        valid = MasterPasswordValidator.valid(password, passwordConfirm)
    }

    LaunchedEffect(valid) {
        onPasswordValidationChange(valid)
    }

    Column(
        modifier = modifier,
    ) {
        TextField(
            value = password,
            onValueChange = onPasswordChange,
            labelText = MdtLocale.strings.masterPasswordLabel,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            visualTransformation = VisualTransformation.SecretField(passwordVisible),
            trailingIcon = {
                SecretFieldTrailingIcon(
                    visible = passwordVisible,
                    onToggle = { passwordVisible = passwordVisible.not() },
                )
            },
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = passwordConfirm,
            onValueChange = { passwordConfirm = it },
            labelText = MdtLocale.strings.masterPasswordConfirmLabel,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = if (valid) {
                KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onDone()
                    },
                )
            } else {
                KeyboardActions.Default
            },
            visualTransformation = VisualTransformation.SecretField(passwordConfirmVisible),
            trailingIcon = {
                SecretFieldTrailingIcon(
                    visible = passwordConfirmVisible,
                    onToggle = { passwordConfirmVisible = passwordConfirmVisible.not() },
                )
            },
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextIcon(
            text = "At least 9 characters",
            style = MdtTheme.typo.regular.xs,
            color = if (lengthValid) MdtTheme.color.primary else MdtTheme.color.onSurfaceVariant,
            leadingIcon = if (lengthValid) MdtIcons.Check else MdtIcons.Close,
            leadingIconTint = if (lengthValid) MdtTheme.color.primary else MdtTheme.color.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        TextIcon(
            text = "Passwords match",
            style = MdtTheme.typo.regular.xs,
            color = if (passwordsMatch) MdtTheme.color.primary else MdtTheme.color.onSurfaceVariant,
            leadingIcon = if (passwordsMatch) MdtIcons.Check else MdtIcons.Close,
            leadingIconTint = if (passwordsMatch) MdtTheme.color.primary else MdtTheme.color.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        MasterPasswordForm(
            modifier = Modifier.fillMaxWidth(),
            password = PreviewText,
        )
    }
}