/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.createmasterpassword

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.headers.ScreenHeader
import com.twofasapp.core.design.feature.password.MasterPasswordForm
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun CreateMasterPasswordScreen(
    viewModel: CreateMasterPasswordViewModel = koinViewModel(),
    openNextStep: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        onPasswordChange = { viewModel.updatePassword(it) },
        onPasswordValidationChange = { viewModel.updatePasswordValidation(it) },
        onEventConsumed = { viewModel.consumeEvent(it) },
        onCtaClick = { viewModel.generateMasterKey() },
        onComplete = openNextStep,
    )
}

@Composable
internal fun Content(
    uiState: CreateMasterPasswordUiState,
    onPasswordChange: (String) -> Unit = {},
    onPasswordValidationChange: (Boolean) -> Unit = {},
    onEventConsumed: (CreateMasterPasswordUiEvent) -> Unit = {},
    onCtaClick: () -> Unit = {},
    onComplete: () -> Unit = {},
) {
    uiState.events.firstOrNull()?.let { event ->
        LaunchedEffect(Unit) {
            when (event) {
                CreateMasterPasswordUiEvent.Complete -> onComplete()
            }

            onEventConsumed(event)
        }
    }

    Scaffold(
        topBar = { TopAppBar() },
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
                    .verticalScroll(rememberScrollState())
                    .weight(1f),
            ) {
                ScreenHeader(
                    title = MdtLocale.strings.generateMasterPasswordTitle,
                    description = MdtLocale.strings.generateMasterPasswordDescription,
                    image = painterResource(com.twofasapp.feature.startup.R.drawable.progress_shield_75),
                )

                Space(32.dp)

                MasterPasswordForm(
                    password = uiState.password,
                    onPasswordChange = onPasswordChange,
                    onPasswordValidationChange = onPasswordValidationChange,
                    onDone = onCtaClick,
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
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.passwordValid,
                    loading = uiState.loading,
                    onClick = onCtaClick,
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            uiState = CreateMasterPasswordUiState(),
        )
    }
}