/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.restorevault.webdav

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
import com.twofasapp.core.design.feature.headers.ScreenHeader
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.dialog.InfoDialog
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.cloudsync.ui.webdav.WebDavForm
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun WebDavRestoreScreen(
    viewModel: WebDavRestoreViewModel = koinViewModel(),
    openRestoreCloudFiles: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var error by remember { mutableStateOf<String?>(null) }

    Content(
        uiState = uiState,
        onUrlChange = viewModel::updateUrl,
        onUsernameChange = viewModel::updateUsername,
        onPasswordChange = viewModel::updatePassword,
        onAllowUntrustedCertificateToggle = viewModel::toggleAllowUntrustedCertificate,
        onCtaClick = {
            viewModel.connect(
                onConnectSuccess = { openRestoreCloudFiles() },
                onConnectFailure = { error = it },
            )
        },
    )

    if (error != null) {
        InfoDialog(
            onDismissRequest = { error = null },
            icon = MdtIcons.Error,
            title = MdtLocale.strings.commonError,
            body = error,
            positive = MdtLocale.strings.commonTryAgain,
        )
    }
}

@Composable
private fun Content(
    uiState: WebDavRestoreUiState,
    onUrlChange: (String) -> Unit = {},
    onUsernameChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onAllowUntrustedCertificateToggle: () -> Unit = {},
    onCtaClick: () -> Unit = {},
) {
    Scaffold(
        topBar = { TopAppBar() },
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
                ScreenHeader(
                    title = MdtLocale.strings.restoreWebdavTitle,
                    description = MdtLocale.strings.restoreWebdavDescription,
                )

                Space(32.dp)

                WebDavForm(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ScreenPadding),
                    url = uiState.url,
                    username = uiState.username,
                    password = uiState.password,
                    allowUntrustedCertificate = uiState.allowUntrustedCertificate,
                    enabled = uiState.loading.not(),
                    onUrlChange = onUrlChange,
                    onUsernameChange = onUsernameChange,
                    onPasswordChange = onPasswordChange,
                    onAllowUntrustedCertificateToggle = onAllowUntrustedCertificateToggle,
                )

                Space(16.dp)
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
                    text = MdtLocale.strings.commonContinue,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.formValid,
                    loading = uiState.loading,
                    onClick = onCtaClick,
                )
            }
        }
    }
}

@Preview
@Composable
fun Preview() {
    PreviewTheme {
        Content(uiState = WebDavRestoreUiState())
    }
}