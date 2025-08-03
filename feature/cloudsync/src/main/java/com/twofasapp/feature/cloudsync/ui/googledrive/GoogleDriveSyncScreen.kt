/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.cloudsync.ui.googledrive

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.feature.settings.OptionHeader
import com.twofasapp.core.design.feature.settings.OptionHeaderContentPaddingFirst
import com.twofasapp.core.design.feature.settings.OptionSwitch
import com.twofasapp.core.design.foundation.button.TextButton
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.cloud.authenticate.AuthenticateCloudService
import com.twofasapp.data.cloud.authenticate.CloudServiceType
import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.feature.cloudsync.ui.common.SyncStatus
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun GoogleDriveSyncScreen(
    viewModel: GoogleDriveSyncViewModel = koinViewModel(),
    goBackToQuickSetup: () -> Unit = {},
    goBackToSync: () -> Unit = {},
    goBackToSettings: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        if (uiState.openedFromQuickSetup) {
            goBackToQuickSetup()
        } else if (uiState.enabled) {
            goBackToSettings()
        } else {
            goBackToSync()
        }
    }

    Content(
        uiState = uiState,
        onCloudAuthenticated = { viewModel.enableSync(it) },
        onDisableSync = { viewModel.disableSync() },
        onSyncClick = { viewModel.sync() },
    )
}

@Composable
private fun Content(
    uiState: GoogleDriveSyncUiState,
    onCloudAuthenticated: (CloudConfig) -> Unit = {},
    onCloudAuthenticationError: (Throwable) -> Unit = {},
    onDisableSync: () -> Unit = {},
    onSyncClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val strings = MdtLocale.strings
    var showCloudAuthentication by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.startAuth) {
        if (uiState.startAuth) {
            showCloudAuthentication = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = strings.settingsEntryCloudSync,
                actions = {
                    if (uiState.enabled) {
                        TextButton(
                            text = "Sync Now",
                            enabled = uiState.syncing.not(),
                            onClick = onSyncClick,
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(top = padding.calculateTopPadding())
                .animateContentSize(),
        ) {
            OptionHeader(
                text = strings.settingsEntryGoogleDrive,
                contentPadding = OptionHeaderContentPaddingFirst,
            )

            OptionSwitch(
                checked = uiState.enabled || showCloudAuthentication,
                title = strings.settingsEntryGoogleDriveSync,
                subtitle = strings.settingsEntryGoogleDriveSyncDesc,
                enabled = uiState.syncing.not(),
                icon = MdtIcons.CloudSync,
                onToggle = { enable ->
                    if (enable) {
                        showCloudAuthentication = true
                    } else {
                        onDisableSync()
                    }
                },
            )

            OptionEntry(
                title = null,
                subtitle = strings.settingsEntryGoogleDriveSyncExplanation,
                subtitleColor = MdtTheme.color.secondary,
            )

            SyncStatus()
        }
    }

    if (showCloudAuthentication) {
        AuthenticateCloudService(
            type = CloudServiceType.GoogleDrive,
            onDismissRequest = { showCloudAuthentication = false },
            onSuccess = onCloudAuthenticated,
            onError = onCloudAuthenticationError,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            uiState = GoogleDriveSyncUiState(),
        )
    }
}