/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.backupdecryption

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.LocalBackDispatcher
import com.twofasapp.core.android.ktx.statusBarHeight
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.main.domain.VaultBackup
import com.twofasapp.feature.importvault.ui.ImportVaultState
import com.twofasapp.feature.importvault.ui.states.DefaultState
import com.twofasapp.feature.importvault.ui.states.EnterSeedState
import com.twofasapp.feature.importvault.ui.states.ErrorState
import com.twofasapp.feature.importvault.ui.states.LoadingState
import com.twofasapp.feature.importvault.ui.states.MasterPasswordState
import com.twofasapp.feature.importvault.ui.states.ScanDecryptionKitState
import com.twofasapp.feature.permissions.RequestPermission
import kotlinx.coroutines.android.awaitFrame
import org.koin.androidx.compose.koinViewModel

@Composable
fun BackupDecryptionModal(
    viewModel: BackupDecryptionViewModel = koinViewModel(),
    onDismissRequest: () -> Unit,
    onSuccess: () -> Unit,
    backup: VaultBackup,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val backDispatcher = LocalBackDispatcher
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.init(backup)
        awaitFrame()
        visible = true
    }

    BackHandler {
        when (uiState.state) {
            is ImportVaultState.Default -> onDismissRequest()
            is ImportVaultState.EnterMasterPassword -> {
                if (uiState.localSeedValid) {
                    onDismissRequest()
                } else {
                    viewModel.openState(ImportVaultState.Default)
                }
            }

            is ImportVaultState.EnterSeed -> viewModel.openState(ImportVaultState.Default)
            is ImportVaultState.ImportingFile -> viewModel.openState(ImportVaultState.Default)
            is ImportVaultState.ImportingFileError -> viewModel.openState(ImportVaultState.Default)
            is ImportVaultState.ImportingFileSuccess -> viewModel.openState(ImportVaultState.Default)
            is ImportVaultState.ReadingFile -> viewModel.openState(ImportVaultState.Default)
            is ImportVaultState.ReadingFileError -> viewModel.openState(ImportVaultState.Default)
            is ImportVaultState.ScanDecryptionKit -> viewModel.openState(ImportVaultState.Default)
            null -> onDismissRequest()
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
            onDecryptionFileLoaded = { viewModel.readDecryptionKit(context, it) },
            onDecryptionFileScanned = { viewModel.readDecryptionKitFromQr(it) },
            onWordsUpdated = { viewModel.updateWords(it) },
            onSeedEntered = { viewModel.restoreFromWords(it) },
            onSeedErrorDismissed = { viewModel.dismissSeedError() },
            onTryDifferentFileClick = {
                viewModel.resetDecryptionKitData()
                backDispatcher?.onBackPressed()
            },
            onTryDifferentDecryptionKitClick = {
                viewModel.resetDecryptionKitData()
                viewModel.openState(ImportVaultState.Default)
            },
            onCheckPassword = { viewModel.checkPassword(it) },
            onFinishWithSuccess = { onSuccess() },
        )
    }
}

@Composable
private fun Content(
    onDismissRequest: () -> Unit,
    uiState: BackupDecryptionUiState,
    onUpdateState: (ImportVaultState) -> Unit = {},
    onDecryptionFileLoaded: (Uri) -> Unit = {},
    onDecryptionFileScanned: (String) -> Unit = {},
    onWordsUpdated: (List<String>) -> Unit = {},
    onSeedEntered: (List<String>) -> Unit = {},
    onSeedErrorDismissed: () -> Unit = {},
    onTryDifferentFileClick: () -> Unit = {},
    onTryDifferentDecryptionKitClick: () -> Unit = {},
    onCheckPassword: (String) -> Unit = {},
    onFinishWithSuccess: () -> Unit = {},
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
                            .align(Alignment.CenterEnd),
                        onClick = onDismissRequest,
                    )
                }
            },
            showBackButton = false,
        )

        if (uiState.state != null) {
            AnimatedContent(
                targetState = uiState.state,
                modifier = Modifier.padding(ScreenPadding),
            ) { state ->
                when (state) {
                    is ImportVaultState.ReadingFile -> {
                        LoadingState(
                            text = MdtLocale.strings.restoreReadingFileText,
                        )
                    }

                    is ImportVaultState.ReadingFileError -> {
                        ErrorState(
                            title = state.title,
                            text = state.msg,
                            onCtaClick = onTryDifferentFileClick,
                        )
                    }

                    is ImportVaultState.Default -> {
                        DefaultState(
                            onDecryptionFileLoaded = onDecryptionFileLoaded,
                            onScanQrClick = { askForCameraPermission = true },
                            onEnterSeedClick = { onUpdateState(ImportVaultState.EnterSeed) },
                        )
                    }

                    is ImportVaultState.ScanDecryptionKit -> {
                        ScanDecryptionKitState(
                            onScanned = onDecryptionFileScanned,
                        )
                    }

                    is ImportVaultState.EnterSeed -> {
                        EnterSeedState(
                            words = uiState.words,
                            seedError = uiState.seedError,
                            onWordsUpdated = onWordsUpdated,
                            onErrorDismissed = onSeedErrorDismissed,
                            onCtaClick = onSeedEntered,
                        )
                    }

                    is ImportVaultState.EnterMasterPassword -> {
                        MasterPasswordState(
                            loading = uiState.passwordLoading,
                            error = uiState.passwordError,
                            onCheckPassword = onCheckPassword,
                        )
                    }

                    is ImportVaultState.ImportingFile -> {
                        LoadingState(
                            text = MdtLocale.strings.restoreImportingFileText,
                        )
                    }

                    is ImportVaultState.ImportingFileError -> {
                        ErrorState(
                            title = state.title,
                            text = state.msg,
                            onCtaClick = onTryDifferentDecryptionKitClick,
                        )
                    }

                    is ImportVaultState.ImportingFileSuccess -> {
                        LaunchedEffect(Unit) {
                            onFinishWithSuccess()
                        }
                    }
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
                onUpdateState(ImportVaultState.ScanDecryptionKit)
                askForCameraPermission = false
            },
            onDismissRequest = { askForCameraPermission = false },
        )
    }
}