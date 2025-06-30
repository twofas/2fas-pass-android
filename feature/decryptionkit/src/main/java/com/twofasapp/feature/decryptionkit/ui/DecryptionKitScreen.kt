/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.decryptionkit.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.headers.ScreenHeader
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.checked.Switch
import com.twofasapp.core.design.foundation.dialog.ConfirmDialog
import com.twofasapp.core.design.foundation.layout.ActionsRow
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.text.TextIcon
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.RoundedShape16
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.decryptionkit.generator.DecryptionKit
import com.twofasapp.feature.decryptionkit.ui.save.DecryptionKitSaveModal
import com.twofasapp.feature.decryptionkit.ui.settings.DecryptionKitSettingsModal

@Composable
fun DecryptionKitScreen(
    decryptionKit: DecryptionKit,
    screenHeaderTitle: String,
    screenHeaderDescription: String,
    screenHeaderImage: Painter? = null,
    screenHeaderIcon: Painter? = null,
    requireSaveConfirmation: Boolean,
    onComplete: () -> Unit = {},
) {
    var showSaveModal by remember { mutableStateOf(false) }
    var showSettingsModal by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var noticeChecked by remember { mutableStateOf(false) }
    var includeMasterKey by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                actions = {
                    ActionsRow {
                        IconButton(
                            icon = MdtIcons.Settings,
                            onClick = { showSettingsModal = true },
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(top = padding.calculateTopPadding(), bottom = ScreenPadding)
                .padding(horizontal = ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ScreenHeader(
                title = screenHeaderTitle,
                description = screenHeaderDescription,
                image = screenHeaderImage,
                icon = screenHeaderIcon,
                iconTint = MdtTheme.color.primary,
            )

            Space(24.dp)

            TextIcon(
                text = MdtLocale.strings.decryptionKitStep1,
                style = MdtTheme.typo.bodyMedium,
                color = MdtTheme.color.onSurfaceVariant,
                leadingIcon = MdtIcons.Download,
                leadingIconTint = MdtTheme.color.onSurfaceVariant,
                leadingIconSize = 18.dp,
                leadingIconSpacer = 4.dp,
            )

            Space(8.dp)

            TextIcon(
                text = MdtLocale.strings.decryptionKitStep2,
                style = MdtTheme.typo.bodyMedium,
                color = MdtTheme.color.onSurfaceVariant,
                leadingIcon = MdtIcons.Print,
                leadingIconTint = MdtTheme.color.onSurfaceVariant,
                leadingIconSize = 18.dp,
                leadingIconSpacer = 4.dp,
            )

            Space(1f)

            Image(
                painter = if (includeMasterKey) {
                    painterResource(com.twofasapp.feature.decryptionkit.R.drawable.img_decryption_kit_file_with_master_key)
                } else {
                    painterResource(com.twofasapp.feature.decryptionkit.R.drawable.img_decryption_kit_file_no_master_key)
                },
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(0.6f),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedShape16)
                    .background(MdtTheme.color.surfaceContainer)
                    .clickable { noticeChecked = noticeChecked.not() }
                    .padding(16.dp),
            ) {
                Icon(
                    contentDescription = null,
                    painter = MdtIcons.Warning,
                    tint = MdtTheme.color.primary,
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                ) {
                    Text(
                        text = MdtLocale.strings.decryptionKitNoticeTitle,
                        style = MdtTheme.typo.titleMedium,
                    )

                    Space(2.dp)

                    Text(
                        text = MdtLocale.strings.decryptionKitNotice,
                        style = MdtTheme.typo.bodyMedium,
                        color = MdtTheme.color.onSurfaceVariant,
                    )
                }

                Switch(
                    checked = noticeChecked,
                    onCheckedChange = { noticeChecked = noticeChecked.not() },
                )
            }

            Space(16.dp)

            Button(
                text = MdtLocale.strings.decryptionKitCta,
                onClick = { showSaveModal = true },
                enabled = noticeChecked,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (showSaveModal) {
        DecryptionKitSaveModal(
            onDismissRequest = { showSaveModal = false },
            decryptionKit = decryptionKit,
            includeMasterKey = includeMasterKey,
            onFileSaved = { onComplete() },
            onShareTriggered = {
                if (requireSaveConfirmation) {
                    showConfirmDialog = true
                }
            },
        )
    }

    if (showSettingsModal) {
        DecryptionKitSettingsModal(
            onDismissRequest = { showSettingsModal = false },
            includeMasterKey = includeMasterKey,
            onIncludeMasterKeyToggle = { includeMasterKey = includeMasterKey.not() },
        )
    }

    if (showConfirmDialog) {
        ConfirmDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = MdtLocale.strings.decryptionKitConfirmTitle,
            body = MdtLocale.strings.decryptionKitConfirmDescription,
            icon = MdtIcons.DecryptionKit,
            onPositive = { onComplete() },
            shouldAutoHideOnLock = false,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        DecryptionKitScreen(
            decryptionKit = DecryptionKit.Empty,
            screenHeaderTitle = MdtLocale.strings.decryptionKitTitle,
            screenHeaderDescription = MdtLocale.strings.decryptionKitDescription,
            screenHeaderImage = MdtIcons.Placeholder,
            requireSaveConfirmation = true,
        )
    }
}

@Preview
@Composable
private fun PreviewNoNotice() {
    PreviewTheme {
        DecryptionKitScreen(
            decryptionKit = DecryptionKit.Empty,
            screenHeaderTitle = MdtLocale.strings.decryptionKitTitle,
            screenHeaderDescription = MdtLocale.strings.decryptionKitDescription,
            screenHeaderImage = MdtIcons.Placeholder,
            requireSaveConfirmation = false,
        )
    }
}