/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.connect.ui.connect

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.settingsIntent
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonHeight
import com.twofasapp.core.design.foundation.button.ButtonStyle
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.dialog.InputDialog
import com.twofasapp.core.design.foundation.layout.ActionsRow
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.connect.ui.intro.ConnectIntroModal
import com.twofasapp.feature.permissions.PermissionStatus
import com.twofasapp.feature.permissions.RequestPermission
import com.twofasapp.feature.permissions.rememberPermissionStatus
import com.twofasapp.feature.purchases.PurchasesDialog
import com.twofasapp.feature.qrscan.QrScan
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun ConnectScreen(
    viewModel: ConnectViewModel = koinViewModel(),
    onOpenHome: () -> Unit,
    onGoBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState.connectOnboardingPrompted) {
        false -> {
            ConnectIntroModal(
                onDismissRequest = {
                    viewModel.setOnboardingPrompted(true)
                },
            )
        }

        true -> {
            Content(
                uiState = uiState,
                onScanned = {
                    viewModel.scanned(it) {
                        onOpenHome()
                    }
                },
                onEnableScanner = { enable ->
                    viewModel.enableScanner(enable)
                },
                onOpenHome = { onGoBack() },
            )
        }

        null -> Unit
    }
}

@Composable
private fun Content(
    uiState: ConnectUiState,
    onScanned: (String) -> Unit = {},
    onEnableScanner: (Boolean) -> Unit = {},
    onOpenHome: () -> Unit = {},
) {
    val strings = MdtLocale.strings
    val context = LocalContext.current
    var showBrowserQrInputDialog by remember { mutableStateOf(false) }
    var askForCameraPermission by remember { mutableStateOf(true) }
    var askForPushPermission by remember { mutableStateOf(false) }
    var showPaywall by remember { mutableStateOf(false) }
    val cameraPermissionState = rememberPermissionStatus(Manifest.permission.CAMERA)

    LifecycleResumeEffect(Unit) {
        onEnableScanner(true)
        onPauseOrDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                content = { Text(text = strings.connectTitle, style = MdtTheme.typo.medium.xl2) },
                showBackButton = false,
                actions = {
                    if (uiState.debuggable) {
                        ActionsRow {
                            IconButton(
                                icon = MdtIcons.Keyboard,
                                onClick = { showBrowserQrInputDialog = true },
                            )
                        }
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
                .padding(horizontal = ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black),
            ) {
                if (uiState.scannerEnabled && askForCameraPermission.not() && askForPushPermission.not()) {
                    QrScan(
                        modifier = Modifier.fillMaxSize(),
                        requireAuth = true,
                        onScanned = { onScanned(it) },
                    )
                }

                if (cameraPermissionState.status is PermissionStatus.Denied) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = strings.permissionCameraMsg,
                            color = MdtTheme.color.onSurfaceVariant,
                            style = MdtTheme.typo.regular.sm,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            text = strings.settingsTitle,
                            size = ButtonHeight.Small,
                            style = ButtonStyle.Text,
                            onClick = { context.startActivity(context.settingsIntent) },
                        )
                    }
                }
            }

            Text(
                text = strings.connectQrInstruction,
                color = MdtTheme.color.tertiary,
                style = MdtTheme.typo.regular.sm,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .padding(16.dp)
                    .clip(RoundedShape12)
                    .alpha(if (uiState.scannerEnabled) 1f else 0f),
                textAlign = TextAlign.Center,
            )
        }
    }

    if (askForCameraPermission) {
        RequestPermission(
            permission = Manifest.permission.CAMERA,
            rationaleEnabled = false,
            onGranted = {
                askForCameraPermission = false

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    askForPushPermission = true
                }
            },
            onDenied = {
                askForCameraPermission = false

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    askForPushPermission = true
                }
            },
            onDismissRequest = {
                askForCameraPermission = false

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    askForPushPermission = true
                }
            },
        )
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (askForPushPermission) {
            RequestPermission(
                permission = Manifest.permission.POST_NOTIFICATIONS,
                rationaleEnabled = false,
                onGranted = {
                    askForPushPermission = false
                },
                onDenied = {
                    askForPushPermission = false
                },
                onDismissRequest = {
                    askForPushPermission = false
                },
            )
        }
    }

    if (showBrowserQrInputDialog) {
        InputDialog(
            onDismissRequest = { showBrowserQrInputDialog = false },
            title = MdtLocale.strings.connectEnterBrowserQrTitle,
            onPositive = { onScanned(it) },
        )
    }

    if (showPaywall) {
        PurchasesDialog(
            onDismissRequest = { showPaywall = false },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            uiState = ConnectUiState(),
        )
    }
}