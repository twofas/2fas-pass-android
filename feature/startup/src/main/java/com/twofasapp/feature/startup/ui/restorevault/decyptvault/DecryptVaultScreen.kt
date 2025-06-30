/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.restorevault.decyptvault

import android.Manifest
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.LocalBackDispatcher
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.importvault.ui.ImportVaultState
import com.twofasapp.feature.importvault.ui.states.DefaultState
import com.twofasapp.feature.importvault.ui.states.EnterSeedState
import com.twofasapp.feature.importvault.ui.states.ErrorState
import com.twofasapp.feature.importvault.ui.states.LoadingState
import com.twofasapp.feature.importvault.ui.states.MasterPasswordState
import com.twofasapp.feature.importvault.ui.states.ScanDecryptionKitState
import com.twofasapp.feature.importvault.ui.states.SuccessState
import com.twofasapp.feature.permissions.RequestPermission
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun DecryptVaultScreen(
    viewModel: DecryptVaultViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val backDispatcher = LocalBackDispatcher
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = uiState.screenState == ImportVaultState.ImportingFileSuccess) {
        viewModel.finishWithSuccess()
    }

    Content(
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
        onFinishWithSuccess = { viewModel.finishWithSuccess() },
    )
}

@Composable
private fun Content(
    uiState: DecryptVaultUiState,
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

    BackHandler(
        when (uiState.screenState) {
            ImportVaultState.EnterSeed -> true
            ImportVaultState.EnterMasterPassword -> true
            ImportVaultState.ScanDecryptionKit -> true
            else -> false
        },
    ) {
        onUpdateState(ImportVaultState.Default)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                showBackButton = uiState.screenState != ImportVaultState.ImportingFileSuccess,
            )
        },
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(top = padding.calculateTopPadding(), bottom = ScreenPadding)
                .padding(horizontal = ScreenPadding),
        ) {
            AnimatedContent(uiState.screenState) { screenState ->
                when (screenState) {
                    is ImportVaultState.ReadingFile -> {
                        LoadingState(
                            text = MdtLocale.strings.restoreReadingFileText,
                        )
                    }

                    is ImportVaultState.ReadingFileError -> {
                        ErrorState(
                            title = screenState.title,
                            text = screenState.msg,
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
                            title = screenState.title,
                            text = screenState.msg,
                            onCtaClick = onTryDifferentDecryptionKitClick,
                        )
                    }

                    is ImportVaultState.ImportingFileSuccess -> {
                        SuccessState(
                            onCtaClick = onFinishWithSuccess,
                        )
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

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            uiState = DecryptVaultUiState(
                screenState = ImportVaultState.Default,
            ),
        )
    }
}