/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.knownbrowsers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.ktx.formatDateTime
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.browsers.Identicon
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.dialog.ConfirmDialog
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.main.domain.ConnectedBrowser
import com.twofasapp.data.main.domain.Identicon
import java.time.Instant

@Composable
internal fun KnownBrowserItem(
    modifier: Modifier = Modifier,
    browser: ConnectedBrowser,
    onDeleteClick: (ConnectedBrowser) -> Unit = {},
) {
    val strings = MdtLocale.strings
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .padding(start = 12.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Identicon(
            modifier = Modifier,
            svgLight = browser.identicon.svgLight,
            svgDark = browser.identicon.svgDark,
            size = 44.dp,
            contentPadding = 7.dp,
        )

        Space(12.dp)

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = browser.extensionName,
                style = MdtTheme.typo.titleMedium,
            )

            Text(
                text = browser.browserName,
                style = MdtTheme.typo.bodySmall,
            )

            Space(4.dp)

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "${strings.knownBrowserLastConnectionPrefix} ${browser.lastSyncAt.formatDateTime()}",
                style = MdtTheme.typo.bodySmall,
                color = MdtTheme.color.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }

        Space(8.dp)

        IconButton(
            icon = MdtIcons.Delete,
            iconTint = MdtTheme.color.outline,
            onClick = { showConfirmDeleteDialog = true },
        )
    }

    if (showConfirmDeleteDialog) {
        ConfirmDialog(
            onDismissRequest = { showConfirmDeleteDialog = false },
            title = strings.knownBrowserDeleteDialogTitle,
            body = strings.knownBrowserDeleteDialogBody,
            icon = MdtIcons.Warning,
            onPositive = { onDeleteClick(browser) },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        KnownBrowserItem(
            browser = ConnectedBrowser(
                id = 1,
                publicKey = byteArrayOf(),
                extensionName = "Extension Name",
                browserName = "Chrome on Windows 11",
                browserVersion = "123",
                identicon = Identicon.Empty,
                createdAt = Instant.now().toEpochMilli(),
                lastSyncAt = Instant.now().toEpochMilli(),
                nextSessionId = byteArrayOf(),
            ),
        )
    }
}