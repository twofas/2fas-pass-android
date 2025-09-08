/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.connect.ui.connectmodal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.openSafely
import com.twofasapp.core.android.ktx.toastShort
import com.twofasapp.core.android.viewmodel.ProvidesViewModelStoreOwner
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonStyle
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.text.TextIcon
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.main.domain.ConnectData
import com.twofasapp.data.main.domain.Identicon
import com.twofasapp.feature.connect.ui.commonmodal.ErrorState
import com.twofasapp.feature.connect.ui.commonmodal.LoadingState
import com.twofasapp.feature.connect.ui.commonmodal.ModalFrame
import com.twofasapp.feature.connect.ui.commonmodal.SuccessState
import org.koin.androidx.compose.koinViewModel

@Composable
fun ConnectModal(
    onDismissRequest: () -> Unit,
    onUpgradePlan: () -> Unit,
    connectData: ConnectData,
) {
    val context = LocalContext.current

    Modal(
        onDismissRequest = onDismissRequest,
        dismissOnBackPress = false,
        dismissOnSwipe = false,
        animateContentSize = true,
        scrimColor = MdtTheme.color.scrim.copy(alpha = 0.9f),
    ) { dismiss ->
        ProvidesViewModelStoreOwner {
            ModalContent(
                connectData = connectData,
                onUpgradePlan = {
                    dismiss {
                        onDismissRequest()
                        onUpgradePlan()
                    }
                },
                onDismissWithToast = { toastMessage ->
                    dismiss {
                        onDismissRequest()
                        context.toastShort(toastMessage)
                    }
                },
                onDismiss = { dismiss { onDismissRequest() } },
            )
        }
    }
}

@Composable
private fun ModalContent(
    viewModel: ConnectModalViewModel = koinViewModel(),
    connectData: ConnectData,
    onUpgradePlan: () -> Unit = {},
    onDismissWithToast: (String) -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(connectData) {
        viewModel.connect(connectData)
    }

    Content(
        uiState = uiState,
        onConfirmConnect = { viewModel.connect(uiState.connectData!!, confirmed = true) },
        onUpgradePlan = onUpgradePlan,
        onDismissWithToast = onDismissWithToast,
        onDismiss = onDismiss,
    )
}

@Composable
private fun Content(
    uiState: ConnectModalUiState,
    onConfirmConnect: () -> Unit = {},
    onUpgradePlan: () -> Unit = {},
    onDismissWithToast: (String) -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val strings = MdtLocale.strings
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(uiState.finishWithSuccess) {
        if (uiState.finishWithSuccess) {
            onDismissWithToast(strings.connectModalSuccessToast)
        }
    }

    ModalFrame(
        title = MdtLocale.strings.connectModalHeaderTitle,
        subtitle = uiState.browserExtensionName,
        identicon = uiState.browserIdenticon,
        onClose = onDismiss,
    ) {
        if (uiState.connectState != null) {
            when (uiState.connectState) {
                is ConnectState.Loading -> {
                    LoadingState(
                        text = MdtLocale.strings.connectModalLoading,
                    )
                }

                is ConnectState.AppUpdateRequired -> {
                    ErrorState(
                        title = MdtLocale.strings.connectModalErrorAppUpdateRequiredTitle,
                        subtitle = MdtLocale.strings.connectModalErrorAppUpdateRequiredSubtitle,
                        cta = MdtLocale.strings.connectModalErrorAppUpdateRequiredCta,
                        onCta = {
                            uriHandler.openSafely(MdtLocale.links.playStore)
                            onDismiss()
                        },
                    )
                }

                is ConnectState.BrowserExtensionUpdateRequired -> {
                    ErrorState(
                        title = MdtLocale.strings.connectModalErrorBrowserExtensionUpdateRequiredTitle,
                        subtitle = MdtLocale.strings.connectModalErrorBrowserExtensionUpdateRequiredSubtitle,
                        cta = MdtLocale.strings.connectModalErrorBrowserExtensionUpdateRequiredCta,
                        onCta = { onDismiss() },
                    )
                }

                is ConnectState.ConfirmNewExtension -> {
                    ConfirmExtensionState(
                        onConfirmConnect = onConfirmConnect,
                        onDismiss = { onDismiss() },
                    )
                }

                is ConnectState.UpgradePlan -> {
                    ErrorState(
                        title = MdtLocale.strings.connectModalErrorExtensionsLimitTitle,
                        subtitle = MdtLocale.strings.connectModalErrorExtensionsLimitSubtitle,
                        cta = MdtLocale.strings.connectModalErrorExtensionsLimitCta,
                        onCta = { onUpgradePlan() },
                    )
                }

                is ConnectState.Success -> {
                    SuccessState(
                        title = MdtLocale.strings.connectModalSuccessTitle,
                        subtitle = MdtLocale.strings.connectModalSuccessSubtitle.format(uiState.browserExtensionName),
                        cta = MdtLocale.strings.connectModalSuccessCta,
                        onCta = { onDismiss() },
                    )
                }

                is ConnectState.Error -> {
                    ErrorState(
                        title = uiState.connectState.title,
                        subtitle = uiState.connectState.subtitle,
                        cta = uiState.connectState.cta,
                        onCta = { onDismiss() },
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfirmExtensionState(
    onConfirmConnect: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column {
        TextIcon(
            text = MdtLocale.strings.connectModalUnknownBrowserTitle,
            style = MdtTheme.typo.titleLarge,
            color = MdtTheme.color.onSurface,
            leadingIcon = MdtIcons.Warning,
            leadingIconSize = 24.dp,
            leadingIconSpacer = 8.dp,
            leadingIconTint = MdtTheme.color.error,
        )

        Space(8.dp)

        Text(
            text = MdtLocale.strings.connectModalUnknownBrowserSubtitle,
            style = MdtTheme.typo.bodyMedium,
            color = MdtTheme.color.onSurfaceVariant,
        )

        Space(20.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                text = MdtLocale.strings.connectModalUnknownBrowserCtaPositive,
                style = ButtonStyle.Tonal,
                containerColor = MdtTheme.color.surfaceContainerHighest,
                onClick = onConfirmConnect,
                modifier = Modifier.weight(1f),
            )

            Button(
                text = MdtLocale.strings.connectModalUnknownBrowserCtaNegative,
                style = ButtonStyle.Tonal,
                onClick = { onDismiss() },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview
@Composable
private fun PreviewLoading() {
    PreviewTheme {
        Content(
            ConnectModalUiState(
                browserExtensionName = "Chrome on Windows",
                browserIdenticon = Identicon.Empty,
                connectState = ConnectState.Loading,
            ),
        )
    }
}

@Preview
@Composable
private fun PreviewConfirmNew() {
    PreviewTheme {
        Content(
            ConnectModalUiState(
                browserExtensionName = "Chrome on Windows",
                browserIdenticon = Identicon.Empty,
                connectState = ConnectState.ConfirmNewExtension,
            ),
        )
    }
}

@Preview
@Composable
private fun PreviewSuccess() {
    PreviewTheme {
        Content(
            ConnectModalUiState(
                browserExtensionName = "Chrome on Windows",
                browserIdenticon = Identicon.Empty,
                connectState = ConnectState.Success,
            ),
        )
    }
}

@Preview
@Composable
private fun PreviewError() {
    PreviewTheme {
        Content(
            ConnectModalUiState(
                browserExtensionName = "Chrome on Windows",
                browserIdenticon = Identicon.Empty,
                connectState = ConnectState.Error(
                    title = "Error",
                    subtitle = "Something went wrong",
                    cta = "Try again",
                ),
            ),
        )
    }
}