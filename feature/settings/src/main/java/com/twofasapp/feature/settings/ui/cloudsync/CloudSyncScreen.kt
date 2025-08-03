/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.cloudsync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.deeplinks.Deeplinks
import com.twofasapp.core.android.navigation.Screen
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.feature.settings.OptionHeader
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.locale.MdtLocale
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun CloudSyncScreen(
    viewModel: CloudSyncViewModel = koinViewModel(),
    deeplinks: Deeplinks = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        onGoogleDriveClick = { deeplinks.openScreen(Screen.GoogleDriveSync(openedFromQuickSetup = false, startAuth = false)) },
        onWebDavClick = { deeplinks.openScreen(Screen.WebDavSync) },
    )
}

@Composable
private fun Content(
    uiState: CloudSyncUiState,
    onGoogleDriveClick: () -> Unit = {},
    onWebDavClick: () -> Unit = {},
) {
    val strings = MdtLocale.strings

    Scaffold(
        topBar = {
            TopAppBar(
                title = strings.settingsEntryCloudSync,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(top = padding.calculateTopPadding()),
        ) {
            OptionEntry(
                title = null,
                subtitle = "Securely sync your 2FAS Pass Vault with Google Drive or WebDav to protect your data if this device gets lost or damaged.",
                contentPadding = PaddingValues(horizontal = 16.dp),
            )

            OptionHeader(
                text = strings.settingsEntryCloudSyncProvider,
            )

            OptionEntry(
                title = strings.settingsEntryGoogleDrive,
                image = painterResource(com.twofasapp.core.design.R.drawable.external_logo_googledrive),
                onClick = { onGoogleDriveClick() },
            )

            OptionEntry(
                title = strings.settingsEntryWebDav,
                icon = MdtIcons.Lan,
                onClick = { onWebDavClick() },
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            uiState = CloudSyncUiState(),
        )
    }
}