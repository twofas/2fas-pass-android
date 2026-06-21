/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.cloudsync.ui.webdav

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
internal fun WebDavSyncScreen(
    viewModel: WebDavSyncViewModel = koinViewModel(),
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
        onUrlChange = viewModel::updateUrl,
        onUsernameChange = viewModel::updateUsername,
        onPasswordChange = viewModel::updatePassword,
        onAllowUntrustedCertificateToggle = viewModel::toggleAllowUntrustedCertificate,
        onConnectClick = viewModel::connect,
    )
}

@Composable
private fun Content(
    uiState: WebDavSyncUiState,
    onUrlChange: (String) -> Unit = {},
    onUsernameChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onAllowUntrustedCertificateToggle: () -> Unit = {},
    onConnectClick: () -> Unit = {},
) {
    val strings = MdtLocale.strings

    Scaffold(
        topBar = {
            TopAppBar(
                title = strings.cloudSyncWebdavTitle,
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
                WebDavForm(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ScreenPadding),
                    url = uiState.url,
                    username = uiState.username,
                    password = uiState.password,
                    allowUntrustedCertificate = uiState.allowUntrustedCertificate,
                    enabled = uiState.connecting.not(),
                    onUrlChange = onUrlChange,
                    onUsernameChange = onUsernameChange,
                    onPasswordChange = onPasswordChange,
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
                    text = if (uiState.configId == null) MdtLocale.strings.webdavConnect else MdtLocale.strings.commonSave,
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
        Content(uiState = WebDavSyncUiState())
    }
}