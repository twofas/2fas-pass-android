/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.restorevault

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.headers.ScreenHeader
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.design.theme.RoundedShape16
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.cloud.authenticate.AuthenticateCloudService
import com.twofasapp.data.cloud.authenticate.CloudServiceType
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun RestoreVaultScreen(
    viewModel: RestoreVaultViewModel = koinViewModel(),
    openRestoreWebDavConfig: () -> Unit = {},
    openRestoreCloudFiles: () -> Unit = {},
    openDecryptVault: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCloudAuthentication by remember { mutableStateOf(false) }
    val backupFilePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.backupFilePicked(it)
            openDecryptVault()
        }
    }

    Content(
        uiState = uiState,
        onGoogleDriveClick = {
            viewModel.updateRestoreSource(RestoreSource.GoogleDrive)
            showCloudAuthentication = true
        },
        onWebDavClick = {
            viewModel.updateRestoreSource(RestoreSource.WebDav)
            openRestoreWebDavConfig()
        },
        onLocalFileClick = {
            viewModel.updateRestoreSource(RestoreSource.LocalFile)
            backupFilePicker.launch("*/*")
        },
    )

    if (showCloudAuthentication) {
        AuthenticateCloudService(
            type = CloudServiceType.GoogleDrive,
            onDismissRequest = { showCloudAuthentication = false },
            onSuccess = {
                viewModel.updateRestoreCloudConfig(it)
                openRestoreCloudFiles()
            },
            onError = {},
        )
    }
}

@Composable
internal fun Content(
    uiState: RestoreVaultUiState,
    onGoogleDriveClick: () -> Unit = {},
    onWebDavClick: () -> Unit = {},
    onLocalFileClick: () -> Unit = {},
) {
    Scaffold(
        topBar = { TopAppBar() },
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(top = padding.calculateTopPadding(), bottom = ScreenPadding)
                .padding(horizontal = ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ScreenHeader(
                title = MdtLocale.strings.restoreVaultSourceTitle,
                description = MdtLocale.strings.restoreVaultSourceDescription,
                icon = MdtIcons.HardDrive,
                iconTint = MdtTheme.color.primary,
            )

            Space(32.dp)

            RestoreSource.entries.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedShape16)
                        .background(MdtTheme.color.surfaceContainer)
                        .clickable {
                            when (item) {
                                RestoreSource.GoogleDrive -> onGoogleDriveClick()
                                RestoreSource.WebDav -> onWebDavClick()
                                RestoreSource.LocalFile -> onLocalFileClick()
                            }
                        }
                        .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedShape12)
                            .background(MdtTheme.color.surface),
                        contentAlignment = Alignment.Center,
                    ) {
                        when (item) {
                            RestoreSource.GoogleDrive -> {
                                Image(
                                    painter = painterResource(com.twofasapp.core.design.R.drawable.external_logo_googledrive),
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                )
                            }

                            RestoreSource.WebDav -> {
                                Icon(
                                    painter = MdtIcons.Cloud,
                                    contentDescription = null,
                                    tint = MdtTheme.color.primary,
                                    modifier = Modifier.size(28.dp),
                                )
                            }

                            RestoreSource.LocalFile -> {
                                Icon(
                                    painter = MdtIcons.Folder,
                                    contentDescription = null,
                                    tint = MdtTheme.color.primary,
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                        }
                    }

                    Space(12.dp)

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (item) {
                                RestoreSource.GoogleDrive -> MdtLocale.strings.restoreVaultSourceOptionGoogleDrive
                                RestoreSource.WebDav -> MdtLocale.strings.restoreVaultSourceOptionWebdav
                                RestoreSource.LocalFile -> MdtLocale.strings.restoreVaultSourceOptionFile
                            },
                            style = MdtTheme.typo.titleMedium,
                        )

                        Text(
                            text = when (item) {
                                RestoreSource.GoogleDrive -> MdtLocale.strings.restoreVaultSourceOptionGoogleDriveDescription
                                RestoreSource.WebDav -> MdtLocale.strings.restoreVaultSourceOptionWebdavDescription
                                RestoreSource.LocalFile -> MdtLocale.strings.restoreVaultSourceOptionFileDescription
                            },
                            style = MdtTheme.typo.bodyMedium,
                            color = MdtTheme.color.onSurfaceVariant,
                        )
                    }

                    Icon(
                        painter = MdtIcons.ChevronRight,
                        contentDescription = null,
                        tint = MdtTheme.color.onSurfaceVariant,
                    )
                }

                Space(12.dp)
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            uiState = RestoreVaultUiState(),
        )
    }
}