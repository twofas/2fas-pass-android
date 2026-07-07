/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.cloudsync

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.locale.MdtLocale

@Composable
internal fun AddProviderModal(
    onDismissRequest: () -> Unit,
    googleDriveEnabled: Boolean = true,
    onGoogleDrive: () -> Unit = {},
    onWebDav: () -> Unit = {},
    onS3: () -> Unit = {},
) {
    Modal(
        onDismissRequest = onDismissRequest,
        headerText = MdtLocale.strings.backupConfigsProviderPickerTitle,
    ) { dismissAction ->
        Content(
            googleDriveEnabled = googleDriveEnabled,
            onGoogleDrive = { dismissAction { onGoogleDrive() } },
            onWebDav = { dismissAction { onWebDav() } },
            onS3 = { dismissAction { onS3() } },
        )
    }
}

@Composable
private fun Content(
    googleDriveEnabled: Boolean = true,
    onGoogleDrive: () -> Unit = {},
    onWebDav: () -> Unit = {},
    onS3: () -> Unit = {},
) {
    val strings = MdtLocale.strings
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        Text(
            text = strings.backupConfigsProviderPickerSubtitle,
            style = MdtTheme.typo.regular.sm,
            color = MdtTheme.color.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Space(16.dp)

        OptionEntry(
            title = strings.backupConfigsProviderGoogleDriveTitle,
            subtitle = if (googleDriveEnabled) {
                strings.backupConfigsProviderGoogleDriveDescription
            } else {
                strings.backupConfigsProviderGoogleDriveNotice
            },
            image = painterResource(com.twofasapp.core.design.R.drawable.external_logo_googledrive),
            enabled = googleDriveEnabled,
            onClick = onGoogleDrive,
        )

        OptionEntry(
            title = strings.backupConfigsProviderWebDavTitle,
            subtitle = strings.backupConfigsProviderWebDavDescription,
            icon = MdtIcons.Lan,
            onClick = onWebDav,
        )

        OptionEntry(
            title = strings.backupConfigsProviderS3Title,
            subtitle = strings.backupConfigsProviderS3Description,
            icon = MdtIcons.Bucket,
            onClick = onS3,
        )

        Space(16.dp)
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content()
    }
}