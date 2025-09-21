/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.importvault.ui.states

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.headers.ScreenHeader
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.textfield.SecretField
import com.twofasapp.core.design.foundation.textfield.SecretFieldTrailingIcon
import com.twofasapp.core.design.foundation.textfield.TextField
import com.twofasapp.core.locale.MdtLocale

@Composable
fun MasterPasswordState(
    loading: Boolean,
    error: String? = null,
    onCheckPassword: (String) -> Unit = {},
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScreenHeader(
            title = MdtLocale.strings.restoreMasterPasswordTitle,
            description = MdtLocale.strings.restoreMasterPasswordDescription,
            icon = MdtIcons.Lock,
            iconTint = MdtTheme.color.primary,
        )

        Space(32.dp)

        TextField(
            value = password,
            onValueChange = { password = it },
            labelText = MdtLocale.strings.restoreMasterPasswordLabel,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            maxLines = 1,
            isError = error != null,
            supportingText = error,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            visualTransformation = VisualTransformation.SecretField(passwordVisible),
            trailingIcon = {
                SecretFieldTrailingIcon(
                    visible = passwordVisible,
                    onToggle = { passwordVisible = passwordVisible.not() },
                )
            },
        )

        Space(1f)

        Button(
            text = MdtLocale.strings.commonContinue,
            loading = loading,
            enabled = password.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            onClick = { onCheckPassword(password) },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        MasterPasswordState(loading = false)
    }
}