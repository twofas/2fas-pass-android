/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.cloudsync.ui.s3

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.menu.DropdownMenu
import com.twofasapp.core.design.foundation.menu.DropdownMenuItem
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.cloudsync.ui.common.SyncStatus
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun S3SyncScreen(
    viewModel: S3SyncViewModel = koinViewModel(),
    goBackToSync: () -> Unit = {},
    goBackToSettings: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        if (uiState.syncEnabled) {
            goBackToSettings()
        } else {
            goBackToSync()
        }
    }

    Content(
        uiState = uiState,
        onEndpointChange = viewModel::updateEndpoint,
        onRegionChange = viewModel::updateRegion,
        onBucketChange = viewModel::updateBucket,
        onAccessKeyIdChange = viewModel::updateAccessKeyId,
        onSecretAccessKeyChange = viewModel::updateSecretAccessKey,
        onAllowUntrustedCertificateToggle = viewModel::toggleAllowUntrustedCertificate,
        onConnectClick = viewModel::connect,
        onDisconnectClick = viewModel::disconnect,
        onSyncClick = viewModel::sync,
    )
}

@Composable
private fun Content(
    uiState: S3SyncUiState,
    onEndpointChange: (String) -> Unit = {},
    onRegionChange: (String) -> Unit = {},
    onBucketChange: (String) -> Unit = {},
    onAccessKeyIdChange: (String) -> Unit = {},
    onSecretAccessKeyChange: (String) -> Unit = {},
    onAllowUntrustedCertificateToggle: () -> Unit = {},
    onConnectClick: () -> Unit = {},
    onDisconnectClick: () -> Unit = {},
    onSyncClick: () -> Unit = {},
) {
    val strings = MdtLocale.strings
    var showDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = strings.cloudSyncS3Title,
                actions = {
                    if (uiState.syncEnabled) {
                        DropdownMenu(
                            visible = showDropdown,
                            onDismissRequest = { showDropdown = false },
                            anchor = {
                                IconButton(
                                    icon = MdtIcons.More,
                                    iconTint = MdtTheme.color.outline,
                                    onClick = {
                                        if (uiState.syncing.not()) {
                                            showDropdown = true
                                        }
                                    },
                                )
                            },
                            content = {
                                DropdownMenuItem(
                                    text = strings.cloudSyncActionSyncNow,
                                    leadingIcon = MdtIcons.Refresh,
                                    onClick = {
                                        onSyncClick()
                                        showDropdown = false
                                    },
                                )

                                DropdownMenuItem(
                                    text = strings.s3Disconnect,
                                    leadingIcon = MdtIcons.Logout,
                                    onClick = {
                                        onDisconnectClick()
                                        showDropdown = false
                                    },
                                )
                            },
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
                .padding(top = padding.calculateTopPadding())
                .imePadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .weight(1f),
            ) {
                S3Form(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ScreenPadding),
                    endpoint = uiState.endpoint,
                    region = uiState.region,
                    bucket = uiState.bucket,
                    accessKeyId = uiState.accessKeyId,
                    secretAccessKey = uiState.secretAccessKey,
                    allowUntrustedCertificate = uiState.allowUntrustedCertificate,
                    enabled = uiState.syncEnabled.not(),
                    onEndpointChange = onEndpointChange,
                    onRegionChange = onRegionChange,
                    onBucketChange = onBucketChange,
                    onAccessKeyIdChange = onAccessKeyIdChange,
                    onSecretAccessKeyChange = onSecretAccessKeyChange,
                    onAllowUntrustedCertificateToggle = onAllowUntrustedCertificateToggle,
                )

                if (uiState.syncEnabled) {
                    SyncStatus()
                }
            }

            if (uiState.syncEnabled.not()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MdtTheme.color.background)
                        .padding(top = 8.dp)
                        .padding(bottom = ScreenPadding)
                        .padding(horizontal = ScreenPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Button(
                        text = MdtLocale.strings.s3Connect,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.formValid,
                        loading = uiState.syncing,
                        onClick = onConnectClick,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(uiState = S3SyncUiState())
    }
}