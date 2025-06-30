/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.compose.BiometricsState
import com.twofasapp.core.android.compose.biometricsState
import com.twofasapp.core.android.deeplinks.Deeplinks
import com.twofasapp.core.android.navigation.Screen
import com.twofasapp.core.common.domain.LoginSecurityType
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.ktx.encodeHex
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.feature.settings.OptionHeader
import com.twofasapp.core.design.feature.settings.OptionHeaderContentPaddingFirst
import com.twofasapp.core.design.feature.settings.OptionSwitch
import com.twofasapp.core.design.foundation.dialog.ConfirmDialog
import com.twofasapp.core.design.foundation.dialog.InfoDialog
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.lock.ui.authentication.AuthenticationPrompt
import com.twofasapp.feature.lock.ui.composables.BiometricsModal
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun SecurityScreen(
    viewModel: SecurityViewModel = koinViewModel(),
    deeplinks: Deeplinks = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        deeplinks = deeplinks,
        onBiometricsChange = { viewModel.updateBiometrics(it) },
        onToggleScreenCapture = { viewModel.toggleScreenCapture() },
        onSaveEncryptedMasterKey = { viewModel.saveMasterKeyEncryptedWithBiometrics(it) },
    )
}

@Composable
private fun Content(
    uiState: SecurityUiState,
    deeplinks: Deeplinks,
    onBiometricsChange: (Boolean) -> Unit = {},
    onToggleScreenCapture: () -> Unit = {},
    onSaveEncryptedMasterKey: (EncryptedBytes?) -> Unit = {},
) {
    val strings = MdtLocale.strings
    val biometricsState = biometricsState()
    var showBiometricsAuthenticationPrompt by remember { mutableStateOf(false) }
    var showScreenCaptureAuthenticationPrompt by remember { mutableStateOf(false) }
    var showScreenCaptureConfirm by remember { mutableStateOf(false) }
    var showLockoutSettingsAuthenticationPrompt by remember { mutableStateOf(false) }
    var showDecryptionKitAuthenticationPrompt by remember { mutableStateOf(false) }
    var showBiometricsModal by remember { mutableStateOf(false) }
    var showBiometricsError by remember { mutableStateOf(false) }
    var biometricsError by remember { mutableStateOf("") }
    var masterKey by remember { mutableStateOf(byteArrayOf()) }

    Scaffold(
        topBar = { TopAppBar(title = strings.settingsEntrySecurity) },
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(top = padding.calculateTopPadding(), bottom = ScreenPadding),
        ) {
            OptionHeader(
                text = strings.settingsEntryAppAccess,
                contentPadding = OptionHeaderContentPaddingFirst,
            )

            OptionEntry(
                title = strings.settingsEntryChangePassword,
                icon = MdtIcons.Password,
                onClick = { deeplinks.openScreen(Screen.ChangePassword) },
            )

            OptionSwitch(
                title = strings.settingsEntryBiometrics,
                subtitle = strings.settingsEntryBiometricsDesc,
                checked = uiState.biometricsEnabled,
                icon = MdtIcons.Fingerprint,
                enabled = biometricsState == BiometricsState.Available,
                onToggle = { checked ->
                    if (checked) {
                        showBiometricsAuthenticationPrompt = true
                    } else {
                        onBiometricsChange(false)
                        onSaveEncryptedMasterKey(null)
                    }
                },
            )

            OptionEntry(
                title = strings.settingsEntryLockoutSettings,
                subtitle = strings.settingsEntryLockoutSettingsDesc,
                icon = MdtIcons.Lock,
                onClick = { showLockoutSettingsAuthenticationPrompt = true },
            )

            OptionHeader(
                text = strings.settingsEntryDataAccess,
            )

            OptionEntry(
                title = strings.settingsEntrySecurityTier,
                subtitle = when (uiState.defaultSecurityType) {
                    LoginSecurityType.Tier1 -> MdtLocale.strings.settingsEntrySecurityTier1
                    LoginSecurityType.Tier2 -> MdtLocale.strings.settingsEntrySecurityTier2
                    LoginSecurityType.Tier3 -> MdtLocale.strings.settingsEntrySecurityTier3
                },
                icon = when (uiState.defaultSecurityType) {
                    LoginSecurityType.Tier1 -> MdtIcons.Tier1
                    LoginSecurityType.Tier2 -> MdtIcons.Tier2
                    LoginSecurityType.Tier3 -> MdtIcons.Tier3
                },
                onClick = { deeplinks.openScreen(Screen.ProtectionLevel) },
            )

            OptionSwitch(
                title = strings.settingsEntryScreenCapture,
                subtitle = strings.settingsEntryScreenCaptureDesc,
                icon = MdtIcons.Screenshot,
                checked = uiState.screenCaptureEnabled,
                onToggle = {
                    if (it) {
                        showScreenCaptureConfirm = true
                    } else {
                        onToggleScreenCapture()
                    }
                },
            )

            OptionHeader(
                text = strings.commonOther,
            )

            OptionEntry(
                title = strings.settingsEntryDecryptionKit,
                icon = MdtIcons.FileSave,
                onClick = { showDecryptionKitAuthenticationPrompt = true },
            )
        }
    }

    if (showBiometricsAuthenticationPrompt) {
        AuthenticationPrompt(
            title = strings.securityBiometricsEnableTitle,
            description = strings.securityBiometricsEnableDescription,
            cta = strings.securityBiometricsEnableCta,
            icon = MdtIcons.Fingerprint,
            biometricsAllowed = false,
            onAuthenticated = {
                masterKey = it
                showBiometricsModal = true
                showBiometricsAuthenticationPrompt = false
            },
            onClose = { showBiometricsAuthenticationPrompt = false },
        )
    }

    if (showLockoutSettingsAuthenticationPrompt) {
        AuthenticationPrompt(
            title = strings.securityLockoutSettingsTitle,
            description = strings.securityLockoutSettingsDescription,
            cta = strings.securityLockoutSettingsCta,
            icon = MdtIcons.Lock,
            biometricsAllowed = true,
            onAuthenticated = {
                showLockoutSettingsAuthenticationPrompt = false
                deeplinks.openScreen(Screen.LockoutSettings)
            },
            onClose = { showLockoutSettingsAuthenticationPrompt = false },
        )
    }

    if (showScreenCaptureAuthenticationPrompt) {
        AuthenticationPrompt(
            title = strings.securityScreenCaptureEnableTitle,
            description = strings.securityScreenCaptureEnableDescription,
            cta = strings.securityScreenCaptureEnableCta,
            icon = MdtIcons.Screenshot,
            biometricsAllowed = true,
            onAuthenticated = {
                showScreenCaptureAuthenticationPrompt = false
                onToggleScreenCapture()
            },
            onClose = { showScreenCaptureAuthenticationPrompt = false },
        )
    }

    if (showDecryptionKitAuthenticationPrompt) {
        AuthenticationPrompt(
            title = strings.securityDecryptionKitAccessTitle,
            description = strings.securityDecryptionKitAccessDescription,
            cta = strings.securityDecryptionKitAccessCta,
            icon = MdtIcons.Lock,
            biometricsAllowed = false,
            onAuthenticated = { masterKey ->
                showDecryptionKitAuthenticationPrompt = false
                deeplinks.openScreen(Screen.SaveDecryptionKit(masterKeyHex = masterKey.encodeHex()))
            },
            onClose = { showDecryptionKitAuthenticationPrompt = false },
        )
    }

    if (showBiometricsModal) {
        BiometricsModal(
            title = strings.biometricsModalTitle,
            subtitle = strings.biometricsModalSubtitleEnable,
            decryptedBytes = masterKey,
            onSuccessEncrypt = { encryptedData ->
                showBiometricsModal = false
                masterKey = byteArrayOf()
                onBiometricsChange(true)
                onSaveEncryptedMasterKey(encryptedData)
            },
            onDismissRequest = {
                showBiometricsModal = false
                masterKey = byteArrayOf()
            },
            onNegativedClick = {
                showBiometricsModal = false
                masterKey = byteArrayOf()
            },
            onError = { code, message ->
                biometricsError = if (code == 7) {
                    strings.biometricsModalErrorTooManyAttempts
                } else {
                    message
                }
                showBiometricsError = true
                masterKey = byteArrayOf()
            },
        )
    }

    if (showBiometricsError) {
        InfoDialog(
            onDismissRequest = { showBiometricsError = false },
            title = strings.biometricsErrorDialogTitle,
            body = biometricsError,
        )
    }

    if (showScreenCaptureConfirm) {
        ConfirmDialog(
            onDismissRequest = { showScreenCaptureConfirm = false },
            title = strings.settingsEntryScreenshotsConfirmTitle,
            body = strings.settingsEntryScreenshotsConfirmDesc,
            icon = MdtIcons.Screenshot,
            onPositive = { showScreenCaptureAuthenticationPrompt = true },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            uiState = SecurityUiState(),
            deeplinks = Deeplinks.Empty,
        )
    }
}