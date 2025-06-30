/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.cloudsync.ui.webdav

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.checked.Switch
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.textfield.Password
import com.twofasapp.core.design.foundation.textfield.PasswordTrailingIcon
import com.twofasapp.core.design.foundation.textfield.TextField
import com.twofasapp.core.locale.MdtLocale

@Composable
fun WebDavForm(
    modifier: Modifier = Modifier,
    url: String,
    username: String,
    password: String,
    allowUntrustedCertificate: Boolean,
    enabled: Boolean,
    onUrlChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onAllowUntrustedCertificateToggle: () -> Unit = {},
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextField(
            value = url,
            onValueChange = onUrlChange,
            labelText = MdtLocale.strings.webdavServerUrl,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Next,
            ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = MdtLocale.strings.webdavAllowUntrustedCertificates,
                style = MdtTheme.typo.labelLarge,
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (enabled) 1f else 0.5f),
            )

            Space(8.dp)

            Switch(
                checked = allowUntrustedCertificate,
                enabled = enabled,
                onCheckedChange = { onAllowUntrustedCertificateToggle() },
            )
        }

        Text(
            text = MdtLocale.strings.webdavCredentials,
            style = MdtTheme.typo.labelLarge,
            color = MdtTheme.color.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (enabled) 1f else 0.5f)
                .padding(vertical = 8.dp),
        )

        TextField(
            value = username,
            onValueChange = onUsernameChange,
            labelText = MdtLocale.strings.webdavUsername,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = enabled,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
        )

        TextField(
            value = password,
            onValueChange = onPasswordChange,
            labelText = MdtLocale.strings.webdavPassword,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                },
            ),
            visualTransformation = VisualTransformation.Password(passwordVisible),
            trailingIcon = {
                if (enabled) {
                    PasswordTrailingIcon(
                        passwordVisible = passwordVisible,
                        onToggle = { passwordVisible = passwordVisible.not() },
                    )
                }
            },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        WebDavForm(
            url = "https://example.com/webdav",
            username = "user",
            password = "password",
            enabled = true,
            allowUntrustedCertificate = false,
            onUrlChange = {},
            onUsernameChange = {},
            onPasswordChange = {},
        )
    }
}