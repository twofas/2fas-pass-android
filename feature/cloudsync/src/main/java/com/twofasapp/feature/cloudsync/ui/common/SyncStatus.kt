/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.cloudsync.ui.common

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.deeplinks.Deeplinks
import com.twofasapp.core.android.ktx.copyToClipboard
import com.twofasapp.core.android.navigation.Screen
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.feature.settings.OptionHeader
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.TextButton
import com.twofasapp.core.design.foundation.dialog.InfoDialog
import com.twofasapp.core.design.foundation.preview.PreviewColumn
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.cloud.exceptions.CloudError
import com.twofasapp.data.cloud.exceptions.asMessage
import com.twofasapp.feature.purchases.PurchasesDialog
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun SyncStatus(
    viewModel: SyncStatusViewModel = koinViewModel(),
    deeplinks: Deeplinks = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        onReSync = { viewModel.sync() },
        onChangePasswordClick = { deeplinks.openScreen(Screen.ChangePassword) },
        onReplaceBackupClick = { viewModel.sync(forceReplace = true) },
    )
}

@Composable
private fun Content(
    uiState: SyncStatusUiState,
    onReSync: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onReplaceBackupClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val strings = MdtLocale.strings
    var showErrorDetailsDialog by remember { mutableStateOf(false) }

    if (uiState.enabled.not()) return

    Column {
        OptionHeader(
            text = strings.settingsEntrySyncInfo,
        )

        when (uiState.config) {
            is CloudConfig.GoogleDrive -> {
                OptionEntry(
                    title = strings.settingsEntrySyncAccount,
                    subtitle = uiState.config.id,
                )
            }

            is CloudConfig.WebDav -> Unit
            else -> Unit
        }

        OptionEntry(
            title = "Status",
            subtitle = uiState.status,
            subtitleColor = if (uiState.error) MdtTheme.color.error else MdtTheme.color.onSurfaceVariant,
        )

        if (uiState.error) {
            ErrorStatus(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                errorType = uiState.cloudError,
                errorCause = uiState.cloudError?.cause,
                errorDetails = uiState.errorDetails,
                onReSync = onReSync,
                onShowErrorDetailsClick = { showErrorDetailsDialog = true },
                onChangePasswordClick = onChangePasswordClick,
                onReplaceBackupClick = onReplaceBackupClick,
            )
        }
    }

    if (showErrorDetailsDialog) {
        InfoDialog(
            onDismissRequest = { showErrorDetailsDialog = false },
            title = uiState.status,
            positive = "Ok",
            negative = "Copy",
            onNegative = { context.copyToClipboard(uiState.errorDetails.orEmpty()) },
            body = uiState.errorDetails,
        )
    }
}

@Composable
private fun ErrorStatus(
    modifier: Modifier = Modifier,
    errorType: CloudError? = null,
    errorCause: Throwable? = null,
    errorDetails: String? = null,
    onReSync: () -> Unit = {},
    onShowErrorDetailsClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onReplaceBackupClick: () -> Unit = {},
) {
    val authLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onReSync()
        }
    }
    var showPaywall by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        errorType?.let {
            Text(
                text = errorType.asMessage(),
                style = MdtTheme.typo.regular.sm,
                color = MdtTheme.color.error,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        when (errorType) {
            is CloudError.Unknown,
            is CloudError.NoNetwork,
            is CloudError.GetFile,
            is CloudError.CreateFile,
            is CloudError.UpdateFile,
            is CloudError.FileParsing,
            is CloudError.NotAuthorized,
            is CloudError.FileIsLocked,
            is CloudError.LocalAccountDoesNotExist,
            is CloudError.CleartextNotPermitted,
            -> {
                if (errorDetails != null) {
                    TextButton(
                        text = "Show error details",
                        modifier = Modifier.padding(top = 8.dp),
                        onClick = onShowErrorDetailsClick,
                    )
                }

                (errorType as? CloudError.NotAuthorized)?.intent?.let { authIntent ->
                    LaunchedEffect(Unit) {
                        authLauncher.launch(authIntent)
                    }
                }
            }

            is CloudError.AuthenticationError -> {
                if (errorDetails != null) {
                    Text(
                        text = errorCause?.message.orEmpty(),
                        style = MdtTheme.typo.regular.sm,
                        color = MdtTheme.color.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    )

                    TextButton(
                        text = "Show error details",
                        modifier = Modifier.padding(top = 4.dp),
                        onClick = onShowErrorDetailsClick,
                    )
                }
            }

            is CloudError.WrongBackupPassword -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        text = "Replace Backup",
                        modifier = Modifier.weight(1f),
                        height = 40.dp,
                        onClick = onReplaceBackupClick,
                    )

                    Button(
                        text = "Change Password",
                        modifier = Modifier.weight(1f),
                        height = 40.dp,
                        onClick = onChangePasswordClick,
                    )
                }
            }

            is CloudError.MultiDeviceSyncNotAvailable -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        text = MdtLocale.strings.paywallNoticeCta,
                        modifier = Modifier.weight(1f),
                        height = 40.dp,
                        onClick = { showPaywall = true },
                    )
                }
            }

            null -> Unit
        }
    }

    if (showPaywall) {
        PurchasesDialog(
            onDismissRequest = { showPaywall = false },
            onSuccess = onReSync,
        )
    }
}

@Preview
@Composable
private fun PreviewError() {
    PreviewColumn {
        ErrorStatus(
            modifier = Modifier.fillMaxWidth(),
            errorType = CloudError.Unknown(null),
        )

        ErrorStatus(
            modifier = Modifier.fillMaxWidth(),
            errorType = CloudError.WrongBackupPassword(null),
        )
    }
}