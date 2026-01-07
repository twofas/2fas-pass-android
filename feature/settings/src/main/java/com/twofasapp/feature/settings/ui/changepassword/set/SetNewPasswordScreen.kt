/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.changepassword.set

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.password.MasterPasswordForm
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.dialog.ConfirmDialog
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.text.richText
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun SetNewPasswordScreen(
    viewModel: SetNewPasswordViewModel = koinViewModel(),
    openProcessingNewPassword: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        onPasswordChange = { viewModel.updatePassword(it) },
        onPasswordValidationChange = { viewModel.updatePasswordValidation(it) },
        onCtaClick = {
            viewModel.proceed { encrypted ->
                openProcessingNewPassword(encrypted)
            }
        },
    )
}

@Composable
private fun Content(
    uiState: SetNewPasswordUiState,
    onPasswordChange: (String) -> Unit = {},
    onPasswordValidationChange: (Boolean) -> Unit = {},
    onCtaClick: () -> Unit = {},
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = MdtLocale.strings.setNewPasswordScreenTitle) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(top = padding.calculateTopPadding(), bottom = ScreenPadding)
                .padding(horizontal = ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = MdtLocale.strings.setNewPasswordScreenDescription,
                    style = MdtTheme.typo.regular.base,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(top = 8.dp, bottom = 24.dp),
                )

                MasterPasswordForm(
                    password = uiState.password,
                    onPasswordChange = onPasswordChange,
                    onPasswordValidationChange = onPasswordValidationChange,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MdtTheme.color.background)
                    .padding(top = 8.dp)
                    .imePadding(),
                contentAlignment = Alignment.Center,
            ) {
                Button(
                    text = MdtLocale.strings.commonContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    enabled = uiState.passwordValid,
                    loading = uiState.loading,
                    onClick = { showConfirmDialog = true },
                )
            }
        }
    }

    if (showConfirmDialog) {
        ConfirmDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = MdtLocale.strings.setNewPasswordConfirmTitle,
            bodyAnnotated = richText(
                buildString {
                    append(MdtLocale.strings.setNewPasswordConfirmBodyPart1Ios)
                    append("\n\n")
                    append(MdtLocale.strings.setNewPasswordConfirmBodyPart2Ios)
                },
            ),
            icon = MdtIcons.Info,
            positive = MdtLocale.strings.commonContinue,
            negative = MdtLocale.strings.commonCancel,
            onPositive = { onCtaClick() },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            uiState = SetNewPasswordUiState(),
        )
    }
}