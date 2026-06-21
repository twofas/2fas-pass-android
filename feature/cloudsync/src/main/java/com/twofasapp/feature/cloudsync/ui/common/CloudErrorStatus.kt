/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.android.ktx.copyToClipboard
import com.twofasapp.core.android.ktx.openSafely
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.TextButton
import com.twofasapp.core.design.foundation.dialog.InfoDialog
import com.twofasapp.core.design.foundation.preview.PreviewColumn
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.cloud.exceptions.CloudError
import com.twofasapp.data.cloud.exceptions.asMessage
import com.twofasapp.feature.purchases.PurchasesDialog

@Composable
fun CloudErrorStatus(
    modifier: Modifier = Modifier,
    errorType: CloudError? = null,
    errorCause: Throwable? = null,
    errorDetails: String? = null,
    errorTitle: String? = null,
    onReSync: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onReplaceBackupClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val authLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onReSync()
        }
    }
    var showPaywall by remember { mutableStateOf(false) }
    var showErrorDetailsDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val strings = MdtLocale.strings

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
                        text = strings.cloudSyncShowErrorDetails,
                        modifier = Modifier.padding(top = 8.dp),
                        onClick = { showErrorDetailsDialog = true },
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
                        text = strings.cloudSyncShowErrorDetails,
                        modifier = Modifier.padding(top = 4.dp),
                        onClick = { showErrorDetailsDialog = true },
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
                        text = strings.cloudSyncActionReplaceBackup,
                        modifier = Modifier.weight(1f),
                        height = 40.dp,
                        onClick = onReplaceBackupClick,
                    )

                    Button(
                        text = strings.cloudSyncActionChangePassword,
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

            is CloudError.InvalidSchemaVersion -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        text = MdtLocale.strings.cloudSyncInvalidSchemaErrorCta,
                        modifier = Modifier.weight(1f),
                        height = 40.dp,
                        onClick = { uriHandler.openSafely(MdtLocale.links.playStore) },
                    )
                }
            }

            null -> Unit
        }
    }

    if (showErrorDetailsDialog) {
        InfoDialog(
            onDismissRequest = { showErrorDetailsDialog = false },
            title = errorTitle ?: errorType.asMessage(),
            positive = strings.commonOk,
            negative = strings.commonCopy,
            onNegative = { context.copyToClipboard(errorDetails.orEmpty()) },
            body = errorDetails,
        )
    }

    if (showPaywall) {
        PurchasesDialog(
            onDismissRequest = { showPaywall = false },
            onSuccess = onReSync,
        )
    }
}

fun Throwable.formatCloudErrorDetails(): String = buildString {
    append("Fatal Exception: ${this@formatCloudErrorDetails.javaClass.name}")
    append("\n")
    append(message)
    append("\n")
    append("\n")
    append(stackTrace.joinToString("\n"))
}

@Preview
@Composable
private fun Preview() {
    PreviewColumn {
        CloudErrorStatus(
            modifier = Modifier.fillMaxWidth(),
            errorType = CloudError.Unknown(null),
        )

        CloudErrorStatus(
            modifier = Modifier.fillMaxWidth(),
            errorType = CloudError.WrongBackupPassword(null),
        )
    }
}