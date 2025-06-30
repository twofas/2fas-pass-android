/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.importexport

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.feature.settings.OptionSwitch
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.preview.PreviewColumn
import com.twofasapp.core.design.foundation.progress.CircularProgress
import com.twofasapp.core.locale.MdtLocale

@Composable
internal fun ExportModal(
    onDismissRequest: () -> Unit,
    onShareClick: (Boolean) -> Unit,
    onSaveToFileClick: (Boolean) -> Unit,
    loading: Boolean,
) {
    Modal(
        onDismissRequest = onDismissRequest,
        headerText = MdtLocale.strings.exportBackupModalTitle,
    ) { dismissAction ->
        Content(
            loading = loading,
            onShareClick = { encrypted -> onShareClick(encrypted) },
            onSaveToFileClick = { encrypted -> onSaveToFileClick(encrypted) },
        )
    }
}

@Composable
private fun Content(
    loading: Boolean,
    onShareClick: (Boolean) -> Unit = {},
    onSaveToFileClick: (Boolean) -> Unit = {},
) {
    val strings = MdtLocale.strings
    var encrypted by remember { mutableStateOf(true) }

    AnimatedContent(loading, label = "content") { state ->
        if (state) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgress()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
            ) {
                OptionSwitch(
                    title = strings.exportBackupModalEncryptedTitle,
                    subtitle = strings.exportBackupModalEncryptedDescription,
                    checked = encrypted,
                    onToggle = { encrypted = encrypted.not() },
                )

                OptionEntry(
                    title = strings.exportBackupModalShare,
                    icon = MdtIcons.Share,
                    onClick = { onShareClick(encrypted) },
                )

                OptionEntry(
                    title = strings.exportBackupModalSaveToFile,
                    icon = MdtIcons.Save,
                    onClick = { onSaveToFileClick(encrypted) },
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewColumn {
        Content(
            loading = true,
        )

        Content(
            loading = false,
        )
    }
}