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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun S3SyncScreen(
    viewModel: S3SyncViewModel = koinViewModel(),
    goBackToSync: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        goBackToSync()
    }

    LaunchedEffect(uiState.closeScreen) {
        if (uiState.closeScreen) {
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
) {
    val strings = MdtLocale.strings

    Scaffold(
        topBar = {
            TopAppBar(
                title = strings.cloudSyncS3Title,
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
                    enabled = uiState.connecting.not(),
                    onEndpointChange = onEndpointChange,
                    onRegionChange = onRegionChange,
                    onBucketChange = onBucketChange,
                    onAccessKeyIdChange = onAccessKeyIdChange,
                    onSecretAccessKeyChange = onSecretAccessKeyChange,
                    onAllowUntrustedCertificateToggle = onAllowUntrustedCertificateToggle,
                )

                if (uiState.error != null) {
                    Text(
                        text = uiState.error,
                        style = MdtTheme.typo.bodyMedium,
                        color = MdtTheme.color.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = ScreenPadding)
                            .padding(top = 12.dp),
                    )
                }
            }

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
                    text = if (uiState.configId == null) MdtLocale.strings.s3Connect else MdtLocale.strings.commonSave,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.formValid,
                    loading = uiState.connecting,
                    onClick = onConnectClick,
                )
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