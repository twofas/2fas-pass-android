/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.connect.ui.requestmodal

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.toastShort
import com.twofasapp.core.android.viewmodel.ProvidesViewModelStoreOwner
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.clearTextOrNull
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.items.ItemEntry
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonStyle
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.text.TextIcon
import com.twofasapp.core.design.theme.RoundedShape16
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.main.domain.BrowserRequestData
import com.twofasapp.data.main.domain.BrowserRequestResponse
import com.twofasapp.data.main.domain.Identicon
import com.twofasapp.feature.connect.ui.commonmodal.ErrorState
import com.twofasapp.feature.connect.ui.commonmodal.LoadingState
import com.twofasapp.feature.connect.ui.commonmodal.LoginFormState
import com.twofasapp.feature.connect.ui.commonmodal.ModalFrame
import com.twofasapp.feature.connect.ui.requestmodal.states.AddItemState
import com.twofasapp.feature.connect.ui.requestmodal.states.AddLoginState
import com.twofasapp.feature.connect.ui.requestmodal.states.DeleteItemState
import com.twofasapp.feature.connect.ui.requestmodal.states.FullSyncState
import com.twofasapp.feature.connect.ui.requestmodal.states.PasswordRequestState
import com.twofasapp.feature.connect.ui.requestmodal.states.SecretFieldRequestState
import com.twofasapp.feature.connect.ui.requestmodal.states.UpdateItemState
import com.twofasapp.feature.connect.ui.requestmodal.states.UpdateLoginState
import org.koin.androidx.compose.koinViewModel

@Composable
fun RequestModal(
    onDismissRequest: () -> Unit,
    onUpgradePlan: () -> Unit,
    requestData: BrowserRequestData,
) {
    val context = LocalContext.current

    Modal(
        onDismissRequest = onDismissRequest,
        dismissOnBackPress = false,
        dismissOnSwipe = false,
        animateContentSize = true,
    ) { dismiss ->
        ProvidesViewModelStoreOwner {
            ModalContent(
                onDismiss = { dismiss { onDismissRequest() } },
                onUpgradePlan = {
                    dismiss {
                        onDismissRequest()
                        onUpgradePlan()
                    }
                },
                onSuccessDismiss = { toastMessage ->
                    dismiss {
                        onDismissRequest()
                        context.toastShort(toastMessage)
                    }
                },
                requestData = requestData,
            )
        }
    }
}

@Composable
private fun ModalContent(
    viewModel: RequestModalViewModel = koinViewModel(),
    onUpgradePlan: () -> Unit = {},
    onSuccessDismiss: (String) -> Unit = {},
    onDismiss: () -> Unit,
    requestData: BrowserRequestData,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val passwordRequestState by viewModel.passwordRequestState.collectAsStateWithLifecycle()
    val fullSyncState by viewModel.fullSyncState.collectAsStateWithLifecycle()
    val secretFieldRequestState by viewModel.secretFieldRequestState.collectAsStateWithLifecycle()
    val deleteItemState by viewModel.deleteItemState.collectAsStateWithLifecycle()
    val addLoginState by viewModel.addLoginState.collectAsStateWithLifecycle()
    val addItemState by viewModel.addItemState.collectAsStateWithLifecycle()
    val updateLoginState by viewModel.updateLoginState.collectAsStateWithLifecycle()
    val updateItemState by viewModel.updateItemState.collectAsStateWithLifecycle()

    LaunchedEffect(requestData) {
        viewModel.connect(requestData)
    }

    Content(
        uiState = uiState,
        passwordRequestState = passwordRequestState,
        fullSyncState = fullSyncState,
        secretFieldRequestState = secretFieldRequestState,
        deleteItemState = deleteItemState,
        addLoginState = addLoginState,
        addItemState = addItemState,
        updateLoginState = updateLoginState,
        updateItemState = updateItemState,
        onUpgradePlan = onUpgradePlan,
        onSuccessDismiss = { onSuccessDismiss(it) },
        onDismiss = {
            viewModel.deleteRequest()
            onDismiss()
        },
    )
}

@Composable
private fun Content(
    uiState: RequestModalUiState,
    passwordRequestState: PasswordRequestState = PasswordRequestState(),
    fullSyncState: FullSyncState = FullSyncState(),
    secretFieldRequestState: SecretFieldRequestState = SecretFieldRequestState(),
    deleteItemState: DeleteItemState = DeleteItemState(),
    addLoginState: AddLoginState = AddLoginState(),
    addItemState: AddItemState = AddItemState(),
    updateLoginState: UpdateLoginState = UpdateLoginState(),
    updateItemState: UpdateItemState = UpdateItemState(),
    onUpgradePlan: () -> Unit = {},
    onSuccessDismiss: (String) -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val strings = MdtLocale.strings

    LaunchedEffect(uiState.finishWithSuccess) {
        if (uiState.finishWithSuccess) {
            val toastMessage = when (uiState.selectedResponse) {
                is BrowserRequestResponse.AddLoginAccept -> strings.requestModalToastAddLogin
                is BrowserRequestResponse.AddItemAccept -> strings.requestModalToastAddLogin
                is BrowserRequestResponse.UpdateLoginAccept -> strings.requestModalToastUpdateLogin
                is BrowserRequestResponse.UpdateItemAccept -> strings.requestModalToastUpdateLogin
                is BrowserRequestResponse.DeleteItemAccept -> strings.requestModalToastDeleteItem
                is BrowserRequestResponse.FullSyncAccept -> strings.connectModalSuccessTitle
                is BrowserRequestResponse.SecretFieldRequestAccept -> strings.requestModalToastPasswordRequest
                is BrowserRequestResponse.PasswordRequestAccept -> strings.requestModalToastPasswordRequest
                is BrowserRequestResponse.Cancel -> strings.requestModalToastCancel
                null -> strings.requestModalToastCancel
            }

            onSuccessDismiss(toastMessage)
        }
    }

    if (uiState.requestState != null) {
        when (uiState.requestState) {
            is RequestState.FullSize -> {
                when (uiState.requestState) {
                    is RequestState.FullSize.ItemForm -> {
                        LoginFormState(
                            itemFormState = uiState.requestState,
                        )
                    }
                }
            }

            is RequestState.InsideFrame -> {
                ModalFrame(
                    title = strings.requestModalHeaderTitle,
                    subtitle = uiState.browserExtensionName,
                    identicon = uiState.browserIdenticon,
                    onClose = onDismiss,
                ) {
                    when (uiState.requestState) {
                        is RequestState.InsideFrame.Loading -> {
                            LoadingState(
                                text = strings.requestModalLoading,
                            )
                        }

                        is RequestState.InsideFrame.FullSync -> {
                            RequestState(
                                item = null,
                                title = strings.requestModalFullSyncTitle,
                                subtitle = strings.requestModalFullSyncSubtitle,
                                icon = MdtIcons.SyncAlt,
                                iconTint = MdtTheme.color.primary,
                                positiveCta = strings.requestModalFullSyncCtaPositive,
                                negativeCta = strings.requestModalFullSyncCtaNegative,
                                onPositiveCta = { fullSyncState.onConfirmClick() },
                                onNegativeCta = { fullSyncState.onCancelClick() },
                            )
                        }

                        is RequestState.InsideFrame.PasswordRequest -> {
                            RequestState(
                                item = passwordRequestState.item,
                                title = strings.requestModalPasswordRequestTitle,
                                subtitle = strings.requestModalPasswordRequestSubtitle,
                                icon = MdtIcons.Downloading,
                                iconTint = MdtTheme.color.primary,
                                positiveCta = strings.requestModalPasswordRequestCtaPositive,
                                negativeCta = strings.requestModalPasswordRequestCtaNegative,
                                onPositiveCta = {
                                    ((passwordRequestState.item.content as? ItemContent.Login)?.password as? SecretField.ClearText)?.value.orEmpty().let {
                                        passwordRequestState.onSendPasswordClick(it)
                                    }
                                },
                                onNegativeCta = {
                                    passwordRequestState.onCancelClick()
                                },
                            )
                        }

                        is RequestState.InsideFrame.AddLogin -> {
                            RequestState(
                                item = addLoginState.item,
                                title = strings.requestModalNewItemTitle,
                                subtitle = strings.requestModalNewItemSubtitle,
                                icon = MdtIcons.AddCircle,
                                iconTint = MdtTheme.color.primary,
                                positiveCta = strings.requestModalNewItemCtaPositive,
                                negativeCta = strings.requestModalNewItemCtaNegative,
                                onPositiveCta = { addLoginState.onContinueClick() },
                                onNegativeCta = { addLoginState.onCancelClick() },
                            )
                        }

                        is RequestState.InsideFrame.UpdateLogin -> {
                            RequestState(
                                item = updateLoginState.item,
                                title = strings.requestModalUpdateItemTitle,
                                subtitle = strings.requestModalUpdateItemSubtitle,
                                icon = MdtIcons.RotateLeft,
                                iconTint = MdtTheme.color.primary,
                                positiveCta = strings.requestModalUpdateItemCtaPositive,
                                negativeCta = strings.requestModalUpdateItemCtaNegative,
                                onPositiveCta = { updateLoginState.onContinueClick() },
                                onNegativeCta = { updateLoginState.onCancelClick() },
                            )
                        }

                        is RequestState.InsideFrame.SecretFieldRequest -> {
                            RequestState(
                                item = secretFieldRequestState.item,
                                title = strings.requestModalPasswordRequestTitle,
                                subtitle = strings.requestModalPasswordRequestSubtitle,
                                icon = MdtIcons.Downloading,
                                iconTint = MdtTheme.color.primary,
                                positiveCta = strings.requestModalPasswordRequestCtaPositive,
                                negativeCta = strings.requestModalPasswordRequestCtaNegative,
                                onPositiveCta = {
                                    val secretFieldsMap = buildMap {
                                        when (val content = secretFieldRequestState.item.content) {
                                            is ItemContent.Login -> {
                                                put("s_password", content.password.clearTextOrNull.orEmpty())
                                            }

                                            is ItemContent.SecureNote -> {
                                                put("s_text", content.text.clearTextOrNull.orEmpty())
                                            }

                                            is ItemContent.Unknown -> Unit
                                        }
                                    }

                                    secretFieldRequestState.onSendClick(secretFieldsMap)
                                },
                                onNegativeCta = {
                                    secretFieldRequestState.onCancelClick()
                                },
                            )
                        }

                        is RequestState.InsideFrame.DeleteItem -> {
                            RequestState(
                                item = deleteItemState.item,
                                title = strings.requestModalRemoveItemTitle,
                                subtitle = strings.requestModalRemoveItemSubtitle,
                                icon = MdtIcons.Delete,
                                iconTint = MdtTheme.color.error,
                                positiveCta = strings.requestModalRemoveItemCtaPositive,
                                negativeCta = strings.requestModalRemoveItemCtaNegative,
                                onPositiveCta = { deleteItemState.onDeleteClick() },
                                onNegativeCta = { deleteItemState.onCancelClick() },
                            )
                        }

                        is RequestState.InsideFrame.AddItem -> {
                            RequestState(
                                item = addItemState.item,
                                title = strings.requestModalNewItemTitle,
                                subtitle = strings.requestModalNewItemSubtitle,
                                icon = MdtIcons.AddCircle,
                                iconTint = MdtTheme.color.primary,
                                positiveCta = strings.requestModalNewItemCtaPositive,
                                negativeCta = strings.requestModalNewItemCtaNegative,
                                onPositiveCta = { addItemState.onContinueClick() },
                                onNegativeCta = { addItemState.onCancelClick() },
                            )
                        }

                        is RequestState.InsideFrame.UpdateItem -> {
                            RequestState(
                                item = updateItemState.item,
                                title = strings.requestModalUpdateItemTitle,
                                subtitle = strings.requestModalUpdateItemSubtitle,
                                icon = MdtIcons.RotateLeft,
                                iconTint = MdtTheme.color.primary,
                                positiveCta = strings.requestModalUpdateItemCtaPositive,
                                negativeCta = strings.requestModalUpdateItemCtaNegative,
                                onPositiveCta = { updateItemState.onContinueClick() },
                                onNegativeCta = { updateItemState.onCancelClick() },
                            )
                        }

                        is RequestState.InsideFrame.UpgradePlan -> {
                            ErrorState(
                                title = strings.requestModalErrorItemsLimitTitle,
                                subtitle = strings.requestModalErrorItemsLimitSubtitle.format(uiState.requestState.maxItems),
                                cta = MdtLocale.strings.requestModalErrorItemsLimitCta,
                                onCta = { onUpgradePlan() },
                            )
                        }

                        is RequestState.InsideFrame.Error -> {
                            ErrorState(
                                title = uiState.requestState.title,
                                subtitle = uiState.requestState.subtitle,
                                cta = uiState.requestState.cta,
                                onCta = { onDismiss() },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestState(
    item: Item?,
    title: String,
    subtitle: String,
    icon: Painter,
    iconTint: Color,
    positiveCta: String,
    negativeCta: String,
    onPositiveCta: () -> Unit = {},
    onNegativeCta: () -> Unit = {},
) {
    var positiveLoading by remember { mutableStateOf(false) }
    var negativeLoading by remember { mutableStateOf(false) }
    var positiveEnabled by remember { mutableStateOf(true) }
    var negativeEnabled by remember { mutableStateOf(true) }

    Column {
        TextIcon(
            text = title,
            style = MdtTheme.typo.titleLarge,
            color = MdtTheme.color.onSurface,
            leadingIcon = icon,
            leadingIconSize = 24.dp,
            leadingIconSpacer = 8.dp,
            leadingIconTint = iconTint,
        )

        Space(8.dp)

        Text(
            text = subtitle,
            style = MdtTheme.typo.bodyMedium,
            color = MdtTheme.color.onSurfaceVariant,
        )

        Space(20.dp)

        item?.let {
            ItemEntry(
                item = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MdtTheme.color.outline.copy(alpha = 0.16f), RoundedShape16)
                    .padding(16.dp),
            )

            Space(20.dp)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                text = negativeCta,
                style = ButtonStyle.Tonal,
                containerColor = MdtTheme.color.surfaceContainerHighest,
                loading = negativeLoading,
                enabled = negativeEnabled,
                onClick = {
                    positiveLoading = false
                    negativeLoading = true
                    positiveEnabled = false
                    negativeEnabled = true

                    onNegativeCta()
                },
                modifier = Modifier.weight(1f),
            )

            Button(
                text = positiveCta,
                style = ButtonStyle.Tonal,
                loading = positiveLoading,
                enabled = positiveEnabled,
                onClick = {
                    positiveLoading = true
                    negativeLoading = false
                    positiveEnabled = true
                    negativeEnabled = false

                    onPositiveCta()
                },
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
            RequestModalUiState(
                browserExtensionName = "Chrome on Windows",
                browserIdenticon = Identicon.Empty,
                requestState = RequestState.InsideFrame.Loading,
            ),
        )
    }
}

@Preview
@Composable
private fun PreviewPasswordRequest() {
    PreviewTheme {
        Content(
            RequestModalUiState(
                browserExtensionName = "Chrome on Windows",
                browserIdenticon = Identicon.Empty,
                requestState = RequestState.InsideFrame.PasswordRequest,
            ),
        )
    }
}

@Preview
@Composable
private fun PreviewError() {
    PreviewTheme {
        Content(
            RequestModalUiState(
                browserExtensionName = "Chrome on Windows",
                browserIdenticon = Identicon.Empty,
                requestState = RequestState.InsideFrame.Error(
                    title = "Error",
                    subtitle = "Something went wrong",
                    cta = "Try again",
                ),
            ),
        )
    }
}