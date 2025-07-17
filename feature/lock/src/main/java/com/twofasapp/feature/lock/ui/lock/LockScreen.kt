/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.lock.ui.lock

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.compose.BiometricsState
import com.twofasapp.core.android.compose.biometricsState
import com.twofasapp.core.android.ktx.copyToClipboard
import com.twofasapp.core.android.ktx.currentActivity
import com.twofasapp.core.android.ktx.restartApp
import com.twofasapp.core.common.domain.SelectedTheme
import com.twofasapp.core.design.AppTheme
import com.twofasapp.core.design.LocalAppTheme
import com.twofasapp.core.design.LocalDynamicColors
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.dialog.ConfirmDialog
import com.twofasapp.core.design.foundation.dialog.InfoDialog
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.lock.ui.composables.AuthenticationForm
import com.twofasapp.feature.lock.ui.composables.BiometricsModal
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun LockScreen(
    viewModel: LockViewModel = koinViewModel(),
) {
    val activity = LocalContext.currentActivity
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val biometricsState = biometricsState()
    var masterKey by remember { mutableStateOf(byteArrayOf()) }
    var showBiometricsModal by remember { mutableStateOf(false) }
    var showBiometricsPromptDialog by remember { mutableStateOf(false) }
    var showBiometricsError by remember { mutableStateOf(false) }
    var biometricsHasBeenPrompted by remember { mutableStateOf(false) }
    var biometricsError by remember { mutableStateOf("") }
    val strings = MdtLocale.strings

    BackHandler {
        activity.finish()
    }

    CompositionLocalProvider(
        LocalAppTheme provides when (uiState.selectedTheme) {
            SelectedTheme.Auto -> AppTheme.Auto
            SelectedTheme.Light -> AppTheme.Light
            SelectedTheme.Dark -> AppTheme.Dark
        },
        LocalDynamicColors provides uiState.dynamicColors,
    ) {
        AppTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MdtTheme.color.background,
            ) {
                Content(
                    uiState = uiState,
                    biometricsHasBeenPrompted = biometricsHasBeenPrompted,
                    onMasterKeyDecrypted = { viewModel.unlockWithBiometrics(it) },
                    onBiometricsInvalidated = { viewModel.biometricsInvalidated() },
                    onUnlockClick = {
                        viewModel.unlockWithPassword(it) { key ->
                            if (uiState.biometricsEnabled.not() && uiState.biometricsPrompted.not() && biometricsState == BiometricsState.Available) {
                                masterKey = key
                                biometricsHasBeenPrompted = true
                                showBiometricsPromptDialog = true
                                viewModel.biometricsPrompted()
                            } else {
                                viewModel.finishWithSuccess()
                            }
                        }
                    },
                    onCloseClick = { activity.finish() },
                )

                if (showBiometricsPromptDialog) {
                    ConfirmDialog(
                        onDismissRequest = { showBiometricsPromptDialog = false },
                        title = strings.lockScreenBiometricsPromptTitle,
                        body = strings.lockScreenBiometricsPromptBody,
                        icon = MdtIcons.Fingerprint,
                        shouldAutoHideOnLock = false,
                        onPositive = {
                            showBiometricsPromptDialog = false
                            showBiometricsModal = true
                            biometricsError = ""
                        },
                        onNegative = {
                            showBiometricsPromptDialog = false
                            viewModel.finishWithSuccess()
                        },
                    )
                }

                if (showBiometricsModal) {
                    BiometricsModal(
                        title = strings.lockScreenBiometricsModalTitle,
                        subtitle = strings.lockScreenBiometricsModalSubtitle,
                        decryptedBytes = masterKey,
                        onSuccessEncrypt = { encryptedData ->
                            showBiometricsModal = false
                            masterKey = byteArrayOf()
                            viewModel.finishWithBiometricsEnabled(encryptedData)
                        },
                        onDismissRequest = {
                            showBiometricsModal = false
                            masterKey = byteArrayOf()
                            viewModel.finishWithSuccess()
                        },
                        onNegativedClick = {
                            showBiometricsModal = false
                            masterKey = byteArrayOf()
                            viewModel.finishWithSuccess()
                        },
                        onError = { code, message ->
                            biometricsError = if (code == 7) {
                                strings.lockScreenBiometricsErrorTooManyAttempts
                            } else {
                                message
                            }

                            showBiometricsModal = false
                            showBiometricsError = true
                        },
                    )
                }

                if (showBiometricsError) {
                    InfoDialog(
                        onDismissRequest = {
                            showBiometricsError = false
                            viewModel.finishWithSuccess()
                        },
                        title = strings.lockScreenBiometricsErrorTitle,
                        body = biometricsError,
                        shouldAutoHideOnLock = false,
                    )
                }

                if (uiState.appUpdateError != null) {
                    InfoDialog(
                        onDismissRequest = {},
                        title = strings.migrationErrorTitle,
                        body = strings.migrationErrorBody,
                        icon = MdtIcons.Error,
                        positive = "Restart app",
                        neutral = "Copy error",
                        onPositive = { activity.restartApp() },
                        onNeutral = { activity.copyToClipboard(uiState.appUpdateError!!.stackTraceToString()) },
                        properties = DialogProperties(
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun Content(
    uiState: LockUiState,
    biometricsHasBeenPrompted: Boolean,
    onMasterKeyDecrypted: (ByteArray) -> Unit = {},
    onBiometricsInvalidated: () -> Unit = {},
    onUnlockClick: (String) -> Unit = {},
    onCloseClick: () -> Unit = {},
) {
    val strings = MdtLocale.strings

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
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
                            .align(Alignment.CenterEnd),
                        onClick = onCloseClick,
                    )
                }
            },
            showBackButton = false,
        )

        AuthenticationForm(
            modifier = Modifier.fillMaxSize(),
            title = strings.lockScreenUnlockTitle,
            description = strings.lockScreenUnlockDescription,
            cta = strings.lockScreenUnlockCta,
            biometricsEnabled = uiState.biometricsEnabled && biometricsHasBeenPrompted.not(),
            masterKeyEncryptedWithBiometrics = uiState.masterKeyEncryptedWithBiometrics,
            passwordError = uiState.passwordError,
            loading = uiState.loading,
            enabled = uiState.locked.not(),
            onUnlockClick = onUnlockClick,
            onMasterKeyDecrypted = onMasterKeyDecrypted,
            onBiometricsInvalidated = onBiometricsInvalidated,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            uiState = LockUiState(),
            biometricsHasBeenPrompted = false,
        )
    }
}