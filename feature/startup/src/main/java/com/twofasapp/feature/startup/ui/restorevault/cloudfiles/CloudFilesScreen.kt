/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.restorevault.cloudfiles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.twofasapp.core.design.foundation.dialog.InfoDialog
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.progress.CircularProgress
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.cloud.domain.CloudFileInfo
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun CloudFilesScreen(
    viewModel: CloudFilesViewModel = koinViewModel(),
    openDecryptVault: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var error by remember { mutableStateOf<String?>(null) }

    Content(
        uiState = uiState,
        onFileSelected = {
            viewModel.selectFile(it)
            openDecryptVault()
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
    uiState: CloudFilesUiState,
    onFileSelected: (CloudFileInfo) -> Unit = {},
) {
    Scaffold(
        topBar = { TopAppBar() },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(top = padding.calculateTopPadding()),
        ) {
            if (uiState.loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgress()
                }
                return@Scaffold
            } else if (uiState.files.isEmpty()) {
                ScreenHeader(
                    title = MdtLocale.strings.restoreCloudFilesTitle,
                    description = MdtLocale.strings.restoreCloudFilesEmptyDescription,
                    modifier = Modifier.padding(horizontal = ScreenPadding),
                )
            } else {
                ScreenHeader(
                    title = MdtLocale.strings.restoreCloudFilesTitle,
                    description = MdtLocale.strings.restoreCloudFilesDescription,
                    modifier = Modifier.padding(horizontal = ScreenPadding),
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ScreenPadding)
                        .padding(top = ScreenPadding),
                    contentPadding = PaddingValues(bottom = ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    uiState.files.forEach { file ->
                        item {
                            CloudFilInfoItem(
                                item = file,
                                onClick = { onFileSelected(file) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(uiState = CloudFilesUiState())
    }
}