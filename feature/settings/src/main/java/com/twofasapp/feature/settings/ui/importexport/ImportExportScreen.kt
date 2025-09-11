/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.importexport

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.clearTmpDir
import com.twofasapp.core.android.ktx.currentActivity
import com.twofasapp.core.android.ktx.openSafely
import com.twofasapp.core.android.ktx.showShareFilePicker
import com.twofasapp.core.android.ktx.toastLong
import com.twofasapp.core.android.ktx.toastShort
import com.twofasapp.core.android.viewmodel.ProvidesViewModelStoreOwner
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.feature.settings.OptionHeader
import com.twofasapp.core.design.foundation.dialog.InfoDialog
import com.twofasapp.core.design.foundation.dialog.LoadingDialog
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.main.domain.VaultBackup
import com.twofasapp.feature.lock.ui.authentication.AuthenticationPrompt
import com.twofasapp.feature.purchases.PurchasesDialog
import com.twofasapp.feature.settings.ui.backupdecryption.BackupDecryptionModal
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun ImportExportScreen(
    viewModel: ImportExportViewModel = koinViewModel(),
    openLogins: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var backupContent by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        onDispose { context.clearTmpDir() }
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.readBackup(it) }
    }

    val directoryPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/2faspass+json")) { uri ->
        uri?.let { fileUri ->
            context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                outputStream.write(backupContent.toByteArray(Charsets.UTF_8))
            }

            backupContent = ""
            context.clearTmpDir()
            context.toastShort("Backup file saved!")
        }
    }

    Content(
        uiState = uiState,
        onImportClick = { filePicker.launch("*/*") },
        onTryToImportClick = { viewModel.tryToImport() },
        onImportSuccess = { openLogins() },
        onCancelImport = { viewModel.cancelImport() },
        onExportShareClick = { encrypted ->
            viewModel.generateBackup(encrypted = encrypted) { filename, backup ->
                context.showShareFilePicker(
                    filename = filename,
                    title = "2FAS Pass Backup File",
                    save = { outputStream -> outputStream.write(backup.toByteArray(Charsets.UTF_8)) },
                )
            }
        },
        onExportSaveClick = { encrypted ->
            viewModel.generateBackup(encrypted = encrypted) { filename, backup ->
                backupContent = backup
                directoryPicker.launch(filename)
            }
        },
        onEventConsumed = { viewModel.consumeEvent(it) },
    )
}

@Composable
private fun Content(
    uiState: ImportExportUiState,
    onImportClick: () -> Unit = {},
    onTryToImportClick: () -> Unit = {},
    onImportSuccess: () -> Unit = {},
    onCancelImport: () -> Unit = {},
    onExportShareClick: (Boolean) -> Unit = {},
    onExportSaveClick: (Boolean) -> Unit = {},
    onEventConsumed: (ImportExportUiEvent) -> Unit = {},
) {
    val context = LocalContext.currentActivity
    val strings = MdtLocale.strings
    val uriHandler = LocalUriHandler.current
    var showExportModal by remember { mutableStateOf(false) }
    var exportLoading by remember { mutableStateOf(false) }
    var showInvalidSchemaErrorDialog by remember { mutableStateOf(false) }
    var showBackupDecryptionModal by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showExportAuthenticationPrompt by remember { mutableStateOf(false) }
    var showPaywall by remember { mutableStateOf(false) }

    uiState.events.firstOrNull()?.let { event ->
        LaunchedEffect(Unit) {
            when (event) {
                is ImportExportUiEvent.ShowInvalidSchemaError -> showInvalidSchemaErrorDialog = true
                is ImportExportUiEvent.ShowDecryptionDialog -> showBackupDecryptionModal = true
                is ImportExportUiEvent.ShowErrorDialog -> showErrorDialog = true
                is ImportExportUiEvent.ResetExportModal -> {
                    showExportModal = false
                    exportLoading = false
                }

                is ImportExportUiEvent.ImportSuccess -> {
                    context.toastLong("Import successful!")
                    onImportSuccess()
                }
            }

            onEventConsumed(event)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = strings.settingsEntryImportExport) },
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(top = padding.calculateTopPadding()),
        ) {
            OptionEntry(
                title = null,
                subtitle = "Safely store your Vault data on your device. Choose to import a previous backup to restore your data or export your current data for safekeeping.",
                contentPadding = PaddingValues(horizontal = 16.dp),
            )
            OptionHeader(
                text = strings.settingsEntryImportExport2Pass,
            )

            OptionEntry(
                title = strings.settingsEntryImport2Pass,
                icon = MdtIcons.Import,
                onClick = {
                    if (uiState.isItemsLimitReached) {
                        showPaywall = true
                    } else {
                        onImportClick()
                    }
                },
            )

            OptionEntry(
                title = strings.settingsEntryExport2Pass,
                icon = MdtIcons.Export,
                onClick = { showExportAuthenticationPrompt = true },
            )
        }
    }

    if (showExportModal) {
        ExportModal(
            loading = exportLoading,
            onDismissRequest = { showExportModal = false },
            onShareClick = {
                exportLoading = true
                onExportShareClick(it)
            },
            onSaveToFileClick = {
                exportLoading = true
                onExportSaveClick(it)
            },
        )
    }

    if (showInvalidSchemaErrorDialog) {
        InfoDialog(
            onDismissRequest = { showInvalidSchemaErrorDialog = false },
            icon = MdtIcons.Warning,
            title = "Import error",
            body = strings.importInvalidSchemaErrorMsg.format(uiState.vaultBackupToImport.schemaVersion),
            positive = strings.importInvalidSchemaErrorCta,
            onPositive = { uriHandler.openSafely(MdtLocale.links.playStore) },
        )
    }

    if (showBackupDecryptionModal) {
        ProvidesViewModelStoreOwner {
            BackupDecryptionModal(
                onDismissRequest = { showBackupDecryptionModal = false },
                onSuccess = {
                    showBackupDecryptionModal = false
                    context.toastLong("Import successful!")
                    onImportSuccess()
                },
                backup = uiState.vaultBackupToImport,
            )
        }
    }

    if (showErrorDialog) {
        InfoDialog(
            onDismissRequest = { showErrorDialog = false },
            icon = MdtIcons.Error,
            title = "Error",
            body = "We were unable to read the backup file. It may be corrupt or damaged.",
            positive = "Try again",
        )
    }

    if (showExportAuthenticationPrompt) {
        AuthenticationPrompt(
            title = "Export Backup",
            description = "Authentication is required to export backup file",
            cta = "Authenticate",
            icon = MdtIcons.Export,
            biometricsAllowed = true,
            onAuthenticated = {
                showExportAuthenticationPrompt = false
                showExportModal = true
            },
            onClose = { showExportAuthenticationPrompt = false },
        )
    }

    if (showPaywall) {
        PurchasesDialog(
            title = MdtLocale.strings.paywallNoticeItemsLimitImportTitle,
            body = MdtLocale.strings.paywallNoticeItemsLimitImportMsg.format(uiState.maxItems),
            onDismissRequest = { showPaywall = false },
        )
    }

    if (uiState.importLoading) {
        LoadingDialog(
            onDismissRequest = {},
            onCancelClick = { onCancelImport() },
            title = "Importing...",
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            uiState = ImportExportUiState(),
        )
    }
}