/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.lock.ui.forgotpassword

import android.Manifest
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.statusBarHeight
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.importvault.ui.states.DecryptionKitSource
import com.twofasapp.feature.importvault.ui.states.DefaultState
import com.twofasapp.feature.importvault.ui.states.ErrorState
import com.twofasapp.feature.importvault.ui.states.LoadingState
import com.twofasapp.feature.importvault.ui.states.ScanDecryptionKitState
import com.twofasapp.feature.permissions.RequestPermission
import kotlinx.coroutines.android.awaitFrame
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun ForgotPasswordModal(
    viewModel: ForgotPasswordViewModel = koinViewModel(),
    onDismissRequest: () -> Unit,
    onSuccess: (masterKey: ByteArray) -> Unit,
    onFailedAttempt: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        awaitFrame()
        visible = true
    }

    BackHandler {
        when (uiState.state) {
            is ForgotPasswordState.Default -> onDismissRequest()
            is ForgotPasswordState.Loading -> viewModel.openState(ForgotPasswordState.Default)
            is ForgotPasswordState.Error -> viewModel.openState(ForgotPasswordState.Default)
            is ForgotPasswordState.QrScan -> viewModel.openState(ForgotPasswordState.Default)
        }
    }

    AnimatedVisibility(
        visible = visible,
        exit = slideOutVertically(tween(250)) { it },
        modifier = Modifier
            .fillMaxSize()
            .background(MdtTheme.color.background),
    ) {
        Content(
            onDismissRequest = onDismissRequest,
            uiState = uiState,
            onUpdateState = { viewModel.openState(it) },
            onDecryptionFileLoaded = {
                viewModel.readDecryptionKit(
                    context = context,
                    fileUri = it,
                    onVerified = onSuccess,
                    onError = onFailedAttempt,
                )
            },
            onDecryptionFileScanned = {
                viewModel.verifyDecryptionKit(
                    text = it,
                    onVerified = onSuccess,
                    onError = onFailedAttempt,
                )
            },
            onTryDifferentDecryptionKitClick = { viewModel.openState(ForgotPasswordState.Default) },
        )
    }
}

@Composable
private fun Content(
    onDismissRequest: () -> Unit,
    uiState: ForgotPasswordUiState,
    onUpdateState: (ForgotPasswordState) -> Unit = {},
    onDecryptionFileLoaded: (Uri) -> Unit = {},
    onDecryptionFileScanned: (String) -> Unit = {},
    onTryDifferentDecryptionKitClick: () -> Unit = {},
) {
    var askForCameraPermission by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(statusBarHeight)
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.16f)))),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MdtTheme.color.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
    ) {
        TopAppBar(
            content = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    IconButton(
                        icon = MdtIcons.Close,
                        iconTint = MdtTheme.color.onBackground,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .align(Alignment.CenterEnd)
                            .testTag("forgotPasswordCloseButton"),
                        onClick = onDismissRequest,
                    )
                }
            },
            showBackButton = false,
        )

        AnimatedContent(
            targetState = uiState.state,
            modifier = Modifier.padding(ScreenPadding),
        ) { state ->
            when (state) {
                is ForgotPasswordState.Default -> {
                    DefaultState(
                        title = MdtLocale.strings.forgotPasswordTitle,
                        description = MdtLocale.strings.forgotPasswordDescription,
                        supportedSources = listOf(DecryptionKitSource.LocalFile, DecryptionKitSource.QrScan),
                        onDecryptionFileLoaded = onDecryptionFileLoaded,
                        onScanQrClick = { askForCameraPermission = true },
                    )
                }

                is ForgotPasswordState.QrScan -> {
                    ScanDecryptionKitState(
                        onScanned = onDecryptionFileScanned,
                    )
                }

                is ForgotPasswordState.Loading -> {
                    LoadingState(text = "")
                }

                is ForgotPasswordState.Error -> {
                    ErrorState(
                        title = state.title,
                        text = state.msg,
                        onCtaClick = onTryDifferentDecryptionKitClick,
                    )
                }
            }
        }
    }

    if (askForCameraPermission) {
        RequestPermission(
            permission = Manifest.permission.CAMERA,
            rationaleEnabled = true,
            rationaleTitle = MdtLocale.strings.permissionCameraTitle,
            rationaleText = MdtLocale.strings.permissionCameraMsg,
            onGranted = {
                onUpdateState(ForgotPasswordState.QrScan)
                askForCameraPermission = false
            },
            onDismissRequest = { askForCameraPermission = false },
        )
    }
}