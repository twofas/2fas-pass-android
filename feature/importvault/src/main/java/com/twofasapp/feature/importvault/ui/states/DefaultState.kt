/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.importvault.ui.states

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.headers.ScreenHeader
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.design.theme.RoundedShape16
import com.twofasapp.core.locale.MdtLocale

@Composable
fun DefaultState(
    onDecryptionFileLoaded: (Uri) -> Unit = {},
    onScanQrClick: () -> Unit = {},
    onEnterSeedClick: () -> Unit = {},
) {
    val decryptionFilePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onDecryptionFileLoaded(it) }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScreenHeader(
            title = MdtLocale.strings.restoreDecryptVaultTitle,
            description = MdtLocale.strings.restoreDecryptVaultDescription,
            icon = MdtIcons.Lock,
            iconTint = MdtTheme.color.primary,
        )

        Space(32.dp)

        DecryptionKitSource.entries.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedShape16)
                    .background(MdtTheme.color.surfaceContainer)
                    .clickable {
                        when (item) {
                            DecryptionKitSource.LocalFile -> decryptionFilePicker.launch("*/*")
                            DecryptionKitSource.QrScan -> onScanQrClick()
                            DecryptionKitSource.Manual -> onEnterSeedClick()
                        }
                    }
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedShape12)
                        .background(MdtTheme.color.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    when (item) {
                        DecryptionKitSource.LocalFile -> {
                            Icon(
                                painter = MdtIcons.Folder,
                                contentDescription = null,
                                tint = MdtTheme.color.primary,
                                modifier = Modifier.size(28.dp),
                            )
                        }

                        DecryptionKitSource.QrScan -> {
                            Icon(
                                painter = MdtIcons.QrScanner,
                                contentDescription = null,
                                tint = MdtTheme.color.primary,
                                modifier = Modifier.size(28.dp),
                            )
                        }

                        DecryptionKitSource.Manual -> {
                            Icon(
                                painter = MdtIcons.Keyboard,
                                contentDescription = null,
                                tint = MdtTheme.color.primary,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                    }
                }

                Space(12.dp)

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (item) {
                            DecryptionKitSource.LocalFile -> MdtLocale.strings.restoreDecryptVaultOptionFile
                            DecryptionKitSource.QrScan -> MdtLocale.strings.restoreDecryptVaultOptionScanQr
                            DecryptionKitSource.Manual -> MdtLocale.strings.restoreDecryptVaultOptionManual
                        },
                        style = MdtTheme.typo.titleMedium,
                    )

                    Text(
                        text = when (item) {
                            DecryptionKitSource.LocalFile -> MdtLocale.strings.restoreDecryptVaultOptionFileDescription
                            DecryptionKitSource.QrScan -> MdtLocale.strings.restoreDecryptVaultOptionScanQrDescription
                            DecryptionKitSource.Manual -> MdtLocale.strings.restoreDecryptVaultOptionManualDescription
                        },
                        style = MdtTheme.typo.bodyMedium,
                        color = MdtTheme.color.onSurfaceVariant,
                    )
                }

                Icon(
                    painter = MdtIcons.ChevronRight,
                    contentDescription = null,
                    tint = MdtTheme.color.onSurfaceVariant,
                )
            }

            Space(12.dp)
        }
    }
}

enum class DecryptionKitSource {
    LocalFile, QrScan, Manual
}