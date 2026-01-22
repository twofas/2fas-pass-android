/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.changepassword.processing

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.headers.ScreenHeader
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTextLong
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.progress.CircularProgress
import com.twofasapp.core.design.foundation.progress.CircularProgressSize
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.RoundedShape16
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun ProcessingNewPasswordScreen(
    viewModel: ProcessingNewPasswordViewModel = koinViewModel(),
    onOpenDecryptionKit: (String) -> Unit,
    onClose: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        onOpenDecryptionKit = { uiState.newMasterKeyHex?.let { onOpenDecryptionKit(it) } ?: onClose() },
        onClose = onClose,
    )
}

@Composable
private fun Content(
    uiState: ProcessingNewPasswordUiState,
    onOpenDecryptionKit: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    val strings = MdtLocale.strings
    BackHandler(
        enabled = uiState.step is ProcessingNewPasswordUiState.Step.Processing,
        onBack = {},
    )

    Scaffold(
        topBar = { TopAppBar(showBackButton = false, title = "") },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(top = padding.calculateTopPadding(), bottom = ScreenPadding)
                .padding(horizontal = ScreenPadding),
        ) {
            AnimatedContent(
                modifier = Modifier.weight(1f),
                targetState = uiState.step,
                label = "stepAnimatedContent",
            ) { state ->

                when (state) {
                    is ProcessingNewPasswordUiState.Step.Processing -> {
                        Column {
                            ScreenHeader(
                                title = strings.settingsChangePasswordProcessingTitle,
                                description = strings.settingsChangePasswordProcessingDescription,
                                iconContent = {
                                    CircularProgress(
                                        size = CircularProgressSize.Large,
                                        modifier = Modifier.padding(bottom = 16.dp),
                                    )
                                },
                            )

                            Text(
                                text = uiState.processingMessage.orEmpty(),
                                style = MdtTheme.typo.regular.sm,
                                color = MdtTheme.color.secondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                                    .padding(top = 48.dp),
                            )
                        }
                    }

                    is ProcessingNewPasswordUiState.Step.Error -> {
                        ScreenHeader(
                            title = strings.commonError,
                            description = state.message,
                            icon = MdtIcons.Error,
                        )
                    }

                    is ProcessingNewPasswordUiState.Step.Success -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            ScreenHeader(
                                title = strings.settingsChangePasswordSuccessTitle,
                                description = strings.settingsChangePasswordSuccessDescription,
                                iconContent = {
                                    Image(
                                        painter = painterResource(com.twofasapp.core.design.R.drawable.img_success),
                                        contentDescription = null,
                                        modifier = Modifier.height(100.dp),
                                    )

                                    Space(16.dp)
                                },
                                modifier = Modifier.padding(horizontal = 12.dp),
                            )

                            Space(1f)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedShape16)
                                    .background(MdtTheme.color.surfaceContainer)
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = strings.settingsChangePasswordSuccessCardTitle,
                                        style = MdtTheme.typo.titleMedium,
                                    )
                                    Text(
                                        text = strings.settingsChangePasswordSuccessCardDescription,
                                        style = MdtTheme.typo.bodyMedium,
                                        color = MdtTheme.color.onSurfaceVariant,
                                    )
                                }

                                Image(
                                    painter = painterResource(com.twofasapp.core.design.R.drawable.img_download),
                                    contentDescription = null,
                                    modifier = Modifier.size(52.dp),
                                )
                            }
                        }
                    }

                    null -> Unit
                }
            }

            AnimatedContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                targetState = uiState.step,
                label = "stepCta",
            ) { state ->
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = when (state) {
                        is ProcessingNewPasswordUiState.Step.Processing -> strings.settingsChangePasswordCtaProcessing
                        is ProcessingNewPasswordUiState.Step.Error -> strings.commonTryAgain
                        is ProcessingNewPasswordUiState.Step.Success -> strings.settingsChangePasswordCtaSuccess
                        null -> ""
                    },
                    enabled = when (state) {
                        is ProcessingNewPasswordUiState.Step.Processing -> false
                        is ProcessingNewPasswordUiState.Step.Error -> true
                        is ProcessingNewPasswordUiState.Step.Success -> true
                        null -> false
                    },
                    onClick = {
                        when (state) {
                            is ProcessingNewPasswordUiState.Step.Processing -> onClose()
                            is ProcessingNewPasswordUiState.Step.Error -> onClose()
                            is ProcessingNewPasswordUiState.Step.Success -> onOpenDecryptionKit()
                            null -> Unit
                        }
                    },
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewProcessing() {
    PreviewTheme {
        Content(
            uiState = ProcessingNewPasswordUiState(
                step = ProcessingNewPasswordUiState.Step.Processing,
            ),
        )
    }
}

@Preview
@Composable
private fun PreviewSuccess() {
    PreviewTheme {
        Content(
            uiState = ProcessingNewPasswordUiState(
                step = ProcessingNewPasswordUiState.Step.Success,
            ),
        )
    }
}

@Preview
@Composable
private fun PreviewError() {
    PreviewTheme {
        Content(
            uiState = ProcessingNewPasswordUiState(
                step = ProcessingNewPasswordUiState.Step.Error(PreviewTextLong),
            ),
        )
    }
}