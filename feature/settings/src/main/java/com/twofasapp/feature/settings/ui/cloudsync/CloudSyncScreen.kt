/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.cloudsync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.deeplinks.Deeplinks
import com.twofasapp.core.android.navigation.Screen
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonStyle
import com.twofasapp.core.design.foundation.dialog.ConfirmDialog
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.screen.ScreenEmpty
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.cloud.authenticate.AuthenticateCloudService
import com.twofasapp.data.cloud.authenticate.DefaultCloudServiceType
import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.cloud.domain.CloudConnection
import com.twofasapp.data.cloud.domain.CloudSyncStatus
import com.twofasapp.data.cloud.exceptions.CloudError
import com.twofasapp.feature.cloudsync.ui.googledrive.GoogleDriveInfoDialog
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun CloudSyncScreen(
    viewModel: CloudSyncViewModel = koinViewModel(),
    deeplinks: Deeplinks = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showGoogleDriveInfoDialog by remember { mutableStateOf(false) }

    Content(
        uiState = uiState,
        onConfigEditClick = { config ->
            when (config.connection) {
                is CloudConnection.GoogleDrive -> showGoogleDriveInfoDialog = true
                is CloudConnection.WebDav -> deeplinks.openScreen(Screen.WebDavSync(configId = config.id))
                is CloudConnection.S3 -> deeplinks.openScreen(Screen.S3Sync(configId = config.id))
            }
        },
        onAddGoogleDrive = viewModel::startGoogleAuth,
        onAddWebDav = { deeplinks.openScreen(Screen.WebDavSync(configId = null)) },
        onAddS3 = { deeplinks.openScreen(Screen.S3Sync(configId = null)) },
        onRemoveConfig = viewModel::removeConfig,
        onGoogleAuthDismiss = viewModel::cancelGoogleAuth,
        onGoogleAuthSuccess = viewModel::onGoogleAuthenticated,
        onReSync = { viewModel.sync() },
        onChangePassword = { deeplinks.openScreen(Screen.Security) },
        onReplaceBackup = { viewModel.sync(forceReplace = true) },
    )

    if (showGoogleDriveInfoDialog) {
        GoogleDriveInfoDialog(
            onDismissRequest = { showGoogleDriveInfoDialog = false },
        )
    }
}

@Composable
private fun Content(
    uiState: CloudSyncUiState,
    onConfigEditClick: (CloudConfig) -> Unit = {},
    onAddGoogleDrive: () -> Unit = {},
    onAddWebDav: () -> Unit = {},
    onAddS3: () -> Unit = {},
    onRemoveConfig: (String) -> Unit = {},
    onGoogleAuthDismiss: () -> Unit = {},
    onGoogleAuthSuccess: (CloudConnection.GoogleDrive) -> Unit = {},
    onReSync: () -> Unit = {},
    onChangePassword: () -> Unit = {},
    onReplaceBackup: () -> Unit = {},
) {
    val strings = MdtLocale.strings
    var showProviderChooser by remember { mutableStateOf(false) }
    var configPendingRemoval by remember { mutableStateOf<CloudConfig?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = strings.settingsEntryCloudSync,
                actions = {
                    if (uiState.configs.isNotEmpty()) {
                        Button(
                            text = MdtLocale.strings.cloudSyncActionSyncNow,
                            style = ButtonStyle.Text,
                            onClick = { onReSync() },
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(top = padding.calculateTopPadding()),
        ) {
            if (uiState.configs.isEmpty()) {
                Text(
                    text = strings.settingsCloudSyncDescriptionLong,
                    style = MdtTheme.typo.bodyMedium,
                    color = MdtTheme.color.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )

                ScreenEmpty(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    icon = MdtIcons.CloudSync,
                    text = strings.backupConfigsEmptyDescription,
                    textAlign = TextAlign.Center,
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = strings.settingsCloudSyncDescriptionLong,
                        style = MdtTheme.typo.bodyMedium,
                        color = MdtTheme.color.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )

                    uiState.configs.forEach { config ->
                        ConfigEntry(
                            config = config,
                            onEditClick = { onConfigEditClick(config) },
                            onDeleteClick = { configPendingRemoval = config },
                            onReSync = onReSync,
                            onChangePassword = onChangePassword,
                            onReplaceBackup = onReplaceBackup,
                        )
                    }
                }
            }

            CloudSyncFab(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(ScreenPadding),
                onClick = { showProviderChooser = true },
            )
        }
    }

    configPendingRemoval?.let { config ->
        val providerName = when (config.connection) {
            is CloudConnection.GoogleDrive -> strings.settingsEntryGoogleDrive
            is CloudConnection.WebDav -> (config.connection as CloudConnection.WebDav).url.toUri().host.toString()
            is CloudConnection.S3 -> (config.connection as CloudConnection.S3).endpoint.toUri().host.toString()
        }

        ConfirmDialog(
            onDismissRequest = { configPendingRemoval = null },
            title = strings.backupConfigsRemoveConfirmTitle(providerName),
            body = strings.backupConfigsRemoveConfirmBody,
            icon = MdtIcons.Delete,
            negative = strings.commonCancel,
            positive = strings.commonDelete,
            onPositive = {
                onRemoveConfig(config.id)
                configPendingRemoval = null
            },
        )
    }

    if (showProviderChooser) {
        AddProviderModal(
            onDismissRequest = { showProviderChooser = false },
            googleDriveEnabled = uiState.hasGoogleDrive.not(),
            onGoogleDrive = onAddGoogleDrive,
            onWebDav = onAddWebDav,
            onS3 = onAddS3,
        )
    }

    if (uiState.startGoogleAuth) {
        AuthenticateCloudService(
            type = DefaultCloudServiceType,
            onDismissRequest = onGoogleAuthDismiss,
            onSuccess = onGoogleAuthSuccess,
            onError = { onGoogleAuthDismiss() },
        )
    }
}

@Preview
@Composable
private fun PreviewEmpty() {
    PreviewTheme {
        Content(uiState = CloudSyncUiState())
    }
}

@Preview
@Composable
private fun PreviewAllProviders() {
    PreviewTheme {
        Content(
            uiState = CloudSyncUiState(
                configs = listOf(
                    CloudConfig(
                        id = "gdrive",
                        syncedAt = System.currentTimeMillis() - 120_000,
                        status = CloudSyncStatus.Synced,
                        connection = CloudConnection.GoogleDrive(
                            accountId = "rafal@midnite.com",
                            credentialType = "",
                        ),
                    ),
                    CloudConfig(
                        id = "webdav",
                        syncedAt = 0L,
                        status = CloudSyncStatus.Syncing,
                        connection = CloudConnection.WebDav(
                            url = "https://cloud.example.com/dav",
                            username = "user",
                            password = "",
                            allowUntrustedCertificate = false,
                        ),
                    ),
                    CloudConfig(
                        id = "s3",
                        syncedAt = System.currentTimeMillis() - 60_000,
                        status = CloudSyncStatus.Error(CloudError.WrongBackupPassword(null)),
                        connection = CloudConnection.S3(
                            endpoint = "https://s3.amazonaws.com",
                            region = "us-east-1",
                            bucket = "my-bucket",
                            accessKeyId = "",
                            secretAccessKey = "",
                            allowUntrustedCertificate = false,
                        ),
                    ),
                ),
            ),
        )
    }
}