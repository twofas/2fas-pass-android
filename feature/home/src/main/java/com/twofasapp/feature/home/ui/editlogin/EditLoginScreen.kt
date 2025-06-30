/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.editlogin

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.design.AppTheme
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonStyle
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.loginform.ui.LoginForm
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun EditLoginScreen(
    viewModel: EditLoginViewModel = koinViewModel(),
    close: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        onLoginUpdated = viewModel::updateLogin,
        onIsValidUpdated = viewModel::updateIsValid,
        onHasUnsavedChangesUpdated = viewModel::updateHasUnsavedChanges,
        onSaveClick = { viewModel.save(onComplete = close) },
        onCloseWithoutSaving = close,
    )
}

@Composable
private fun Content(
    uiState: EditLoginUiState,
    onLoginUpdated: (Login) -> Unit = {},
    onIsValidUpdated: (Boolean) -> Unit = {},
    onHasUnsavedChangesUpdated: (Boolean) -> Unit = {},
    onSaveClick: () -> Unit = {},
    onCloseWithoutSaving: () -> Unit = {},
) {
    val strings = MdtLocale.strings

    Scaffold(
        topBar = {
            TopAppBar(
                title = if (uiState.login.id.isBlank()) {
                    strings.loginAddTitle
                } else {
                    strings.loginEditTitle
                },
                actions = {
                    Button(
                        text = strings.commonSave,
                        style = ButtonStyle.Text,
                        onClick = onSaveClick,
                        enabled = uiState.isValid && uiState.hasUnsavedChanges,
                    )
                },
            )
        },
    ) { padding ->
        LoginForm(
            modifier = Modifier.padding(top = padding.calculateTopPadding()),
            initialLogin = uiState.initialLogin,
            onLoginUpdated = onLoginUpdated,
            onIsValidUpdated = onIsValidUpdated,
            onHasUnsavedChangesUpdated = onHasUnsavedChangesUpdated,
            onCloseWithoutSaving = onCloseWithoutSaving,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(appTheme = AppTheme.Dark) {
        Content(
            uiState = EditLoginUiState(
                initialLogin = Login.Preview,
                login = Login.Preview,
            ),
        )
    }
}