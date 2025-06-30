/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.connect.ui.commonmodal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonStyle
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.connect.ui.requestmodal.RequestState
import com.twofasapp.feature.loginform.ui.LoginForm

@Composable
internal fun LoginFormState(
    loginFormState: RequestState.FullSize.LoginForm,
) {
    var login by remember { mutableStateOf(loginFormState.login) }
    var isValid by remember { mutableStateOf(false) }

    BackHandler {
        loginFormState.onCancel()
    }

    Column {
        TopAppBar(
            title = if (loginFormState.login.id.isBlank()) {
                MdtLocale.strings.loginAddTitle
            } else {
                MdtLocale.strings.loginEditTitle
            },
            onBackClick = { loginFormState.onCancel() },
            containerColor = MdtTheme.color.surfaceContainerLow,
            actions = {
                Button(
                    text = MdtLocale.strings.commonSave,
                    style = ButtonStyle.Text,
                    onClick = { loginFormState.onSaveClick(login) },
                    enabled = isValid,
                )
            },
        )

        LoginForm(
            modifier = Modifier.fillMaxSize(),
            initialLogin = loginFormState.login,
            containerColor = MdtTheme.color.surfaceContainerLow,
            confirmUnsavedChanges = false,
            onLoginUpdated = { login = it },
            onIsValidUpdated = { isValid = it },
            onHasUnsavedChangesUpdated = { },
            onCloseWithoutSaving = { loginFormState.onCancel() },
        )
    }
}