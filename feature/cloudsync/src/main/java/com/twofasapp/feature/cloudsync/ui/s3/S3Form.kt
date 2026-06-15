/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.cloudsync.ui.s3

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
import com.twofasapp.core.design.foundation.textfield.SecretField
import com.twofasapp.core.design.foundation.textfield.SecretFieldTrailingIcon
import com.twofasapp.core.design.foundation.textfield.TextField
import com.twofasapp.core.locale.MdtLocale

@Composable
fun S3Form(
    modifier: Modifier = Modifier,
    endpoint: String,
    region: String,
    bucket: String,
    accessKeyId: String,
    secretAccessKey: String,
    allowUntrustedCertificate: Boolean,
    enabled: Boolean,
    onEndpointChange: (String) -> Unit,
    onRegionChange: (String) -> Unit,
    onBucketChange: (String) -> Unit,
    onAccessKeyIdChange: (String) -> Unit,
    onSecretAccessKeyChange: (String) -> Unit,
    onAllowUntrustedCertificateToggle: () -> Unit = {},
) {
    var secretVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextField(
            value = endpoint,
            onValueChange = onEndpointChange,
            labelText = MdtLocale.strings.s3Endpoint,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Next,
            ),
        )

        TextField(
            value = region,
            onValueChange = onRegionChange,
            labelText = MdtLocale.strings.s3Region,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
        )

        TextField(
            value = bucket,
            onValueChange = onBucketChange,
            labelText = MdtLocale.strings.s3Bucket,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = MdtLocale.strings.s3AllowUntrustedCertificates,
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

        Space(8.dp)

        Text(
            text = MdtLocale.strings.s3Credentials,
            style = MdtTheme.typo.labelLarge,
            color = MdtTheme.color.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (enabled) 1f else 0.5f)
                .padding(bottom = 8.dp),
        )

        TextField(
            value = accessKeyId,
            onValueChange = onAccessKeyIdChange,
            labelText = MdtLocale.strings.s3AccessKeyId,
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
            value = secretAccessKey,
            onValueChange = onSecretAccessKeyChange,
            labelText = MdtLocale.strings.s3SecretAccessKey,
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
            visualTransformation = VisualTransformation.SecretField(secretVisible),
            trailingIcon = {
                if (enabled) {
                    SecretFieldTrailingIcon(
                        visible = secretVisible,
                        onToggle = { secretVisible = secretVisible.not() },
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
        S3Form(
            endpoint = "https://s3.example.com",
            region = "us-east-1",
            bucket = "my-bucket",
            accessKeyId = "AKIA...",
            secretAccessKey = "secret",
            allowUntrustedCertificate = false,
            enabled = true,
            onEndpointChange = {},
            onRegionChange = {},
            onBucketChange = {},
            onAccessKeyIdChange = {},
            onSecretAccessKeyChange = {},
        )
    }
}