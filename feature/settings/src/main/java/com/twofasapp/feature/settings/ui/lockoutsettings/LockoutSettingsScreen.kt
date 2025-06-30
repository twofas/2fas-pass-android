/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.lockoutsettings

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
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.feature.settings.OptionHeader
import com.twofasapp.core.design.feature.settings.OptionHeaderContentPaddingFirst
import com.twofasapp.core.design.foundation.dialog.ListRadioDialog
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.settings.domain.AppLockAttempts
import com.twofasapp.data.settings.domain.AppLockTime
import com.twofasapp.data.settings.domain.AutofillLockTime
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun LockoutSettingsScreen(
    viewModel: LockoutSettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        onAppLockTimeChange = viewModel::updateAppLockTime,
        onAppLockAttemptsChange = viewModel::updateAppLockAttempts,
        onAutofillLockTimeChange = viewModel::updateAutofillLockTime,
    )
}

@Composable
private fun Content(
    uiState: LockoutSettingsUiState,
    onAppLockTimeChange: (AppLockTime) -> Unit = {},
    onAppLockAttemptsChange: (AppLockAttempts) -> Unit = {},
    onAutofillLockTimeChange: (AutofillLockTime) -> Unit = {},
) {
    val strings = MdtLocale.strings
    var showAppLockTimeDialog by remember { mutableStateOf(false) }
    var showAppLockAttemptsDialog by remember { mutableStateOf(false) }
    var showAutofillLockTimeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = strings.settingsEntryLockoutSettings) },
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(top = padding.calculateTopPadding()),
        ) {
            OptionHeader(
                text = strings.lockoutSettingsAppLockoutHeader,
                contentPadding = OptionHeaderContentPaddingFirst,
            )
            OptionEntry(
                title = strings.settingsEntryAppLockTime,
                subtitle = uiState.appLockTime.asString(),
                icon = MdtIcons.LockTime,
                onClick = { showAppLockTimeDialog = true },
            )

            OptionEntry(
                title = strings.settingsEntryAppLockAttempts,
                subtitle = uiState.appLockAttempts.asString(),
                icon = MdtIcons.LockAttempts,
                onClick = { showAppLockAttemptsDialog = true },
            )

            OptionHeader(
                text = strings.lockoutSettingsAutofillLockoutHeader,
            )

            OptionEntry(
                title = strings.settingsEntryAutofillLockTime,
                subtitle = uiState.autofillLockTime.asString(),
                icon = MdtIcons.LockKeyboard,
                onClick = { showAutofillLockTimeDialog = true },
            )
        }
    }

    if (showAppLockTimeDialog) {
        ListRadioDialog(
            title = strings.settingsEntryAppLockTime,
            body = strings.settingsEntryAppLockTimeDesc,
            onDismissRequest = { showAppLockTimeDialog = false },
            options = AppLockTime.entries.map { it.asString() },
            selectedIndex = AppLockTime.entries.indexOf(uiState.appLockTime),
            onOptionSelected = { index, _ -> onAppLockTimeChange(AppLockTime.entries[index]) },
        )
    }

    if (showAppLockAttemptsDialog) {
        ListRadioDialog(
            title = strings.settingsEntryAppLockAttempts,
            body = strings.settingsEntryAppLockAttemptsDesc,
            onDismissRequest = { showAppLockAttemptsDialog = false },
            options = AppLockAttempts.entries.map { it.asString() },
            selectedIndex = AppLockAttempts.entries.indexOf(uiState.appLockAttempts),
            onOptionSelected = { index, _ -> onAppLockAttemptsChange(AppLockAttempts.entries[index]) },
        )
    }

    if (showAutofillLockTimeDialog) {
        ListRadioDialog(
            title = strings.settingsEntryAutofillLockTime,
            body = strings.settingsEntryAutofillLockTimeDesc,
            onDismissRequest = { showAutofillLockTimeDialog = false },
            options = AutofillLockTime.entries.map { it.asString() },
            selectedIndex = AutofillLockTime.entries.indexOf(uiState.autofillLockTime),
            onOptionSelected = { index, _ -> onAutofillLockTimeChange(AutofillLockTime.entries[index]) },
        )
    }
}

@Composable
private fun AppLockTime.asString(): String {
    val strings = MdtLocale.strings
    return when (this) {
        AppLockTime.Immediately -> strings.lockoutSettingsAppLockTimeImmediately
        AppLockTime.Seconds30 -> strings.lockoutSettingsAppLockTimeSeconds30
        AppLockTime.Minute1 -> strings.lockoutSettingsAppLockTimeMinute1
        AppLockTime.Minute5 -> strings.lockoutSettingsAppLockTimeMinutes5
        AppLockTime.Hour1 -> strings.lockoutSettingsAppLockTimeHour1
    }
}

@Composable
private fun AppLockAttempts.asString(): String {
    val strings = MdtLocale.strings
    return when (this) {
        AppLockAttempts.Count3 -> strings.lockoutSettingsAppLockAttemptsCount3
        AppLockAttempts.Count5 -> strings.lockoutSettingsAppLockAttemptsCount5
        AppLockAttempts.Count10 -> strings.lockoutSettingsAppLockAttemptsCount10
        AppLockAttempts.NoLimit -> strings.lockoutSettingsAppLockAttemptsNoLimit
    }
}

@Composable
private fun AutofillLockTime.asString(): String {
    val strings = MdtLocale.strings
    return when (this) {
        AutofillLockTime.Minutes5 -> strings.lockoutSettingsAutofillLockTimeMinutes5
        AutofillLockTime.Minutes15 -> strings.lockoutSettingsAutofillLockTimeMinutes15
        AutofillLockTime.Minutes30 -> strings.lockoutSettingsAutofillLockTimeMinutes30
        AutofillLockTime.Hour1 -> strings.lockoutSettingsAutofillLockTimeHour1
        AutofillLockTime.Day1 -> strings.lockoutSettingsAutofillLockTimeDay1
        AutofillLockTime.Never -> strings.lockoutSettingsAutofillLockTimeNever
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            uiState = LockoutSettingsUiState(),
        )
    }
}