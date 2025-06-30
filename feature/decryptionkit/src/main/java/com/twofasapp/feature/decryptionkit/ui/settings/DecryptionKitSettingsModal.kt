/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.decryptionkit.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.checked.Switch
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.theme.RoundedShape16
import com.twofasapp.core.locale.MdtLocale

@Composable
fun DecryptionKitSettingsModal(
    onDismissRequest: () -> Unit,
    includeMasterKey: Boolean,
    onIncludeMasterKeyToggle: () -> Unit = {},
) {
    Modal(
        onDismissRequest = onDismissRequest,
        headerText = MdtLocale.strings.decryptionKitSettingsTitle,
    ) { dismiss ->
        Content(
            includeMasterKey = includeMasterKey,
            onIncludeMasterKeyToggle = onIncludeMasterKeyToggle,
            onDoneClick = { dismiss {} },
        )
    }
}

@Composable
private fun Content(
    includeMasterKey: Boolean,
    onIncludeMasterKeyToggle: () -> Unit = {},
    onDoneClick: () -> Unit = {},
) {
    val strings = MdtLocale.strings

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = strings.decryptionKitSettingsDescription,
            style = MdtTheme.typo.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )

        Space(12.dp)

        Image(
            painter = if (includeMasterKey) {
                painterResource(com.twofasapp.feature.decryptionkit.R.drawable.img_decryption_kit_qr_with_master_key)
            } else {
                painterResource(com.twofasapp.feature.decryptionkit.R.drawable.img_decryption_kit_qr_no_master_key)
            },
            contentDescription = null,
            modifier = Modifier.size(164.dp),
        )

        Text(
            text = strings.decryptionKitSettingsQrLabel,
            style = MdtTheme.typo.bodyMedium,
            color = MdtTheme.color.onSurfaceVariant,
        )

        Space(12.dp)

        Icon(
            painter = painterResource(com.twofasapp.feature.decryptionkit.R.drawable.img_decryption_kit_qr_arrow),
            contentDescription = null,
            tint = MdtTheme.color.primary,
            modifier = Modifier.size(24.dp),
        )

        Space(14.dp)

        Text(
            text = strings.decryptionKitSettingsSecretWords,
            style = MdtTheme.typo.labelLargeProminent,
        )

        Space(8.dp)

        Text(
            text = buildAnnotatedString {
                if (includeMasterKey) {
                    append(strings.decryptionKitSettingsMasterKey)
                } else {
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough, color = MdtTheme.color.onSurfaceVariant)) {
                        append(strings.decryptionKitSettingsMasterKey)
                    }
                }
            },
            style = MdtTheme.typo.labelLargeProminent,
        )

        Space(32.dp)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedShape16)
                .background(MdtTheme.color.surfaceContainer)
                .clickable { onIncludeMasterKeyToggle() }
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
            ) {
                androidx.compose.material3.Text(
                    text = strings.decryptionKitSettingsToggleTitle,
                    style = MdtTheme.typo.titleMedium,
                )

                Space(2.dp)

                androidx.compose.material3.Text(
                    text = strings.decryptionKitSettingsToggleMsg,
                    style = MdtTheme.typo.bodyMedium,
                    color = MdtTheme.color.onSurfaceVariant,
                )
            }

            Switch(
                checked = includeMasterKey,
                onCheckedChange = { onIncludeMasterKeyToggle() },
            )
        }

        Space(24.dp)

        Button(
            text = strings.decryptionKitSettingsCta,
            onClick = onDoneClick,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            includeMasterKey = true,
        )
    }
}