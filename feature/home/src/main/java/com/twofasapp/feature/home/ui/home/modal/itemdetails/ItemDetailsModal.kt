/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.home.modal.itemdetails

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.copyToClipboard
import com.twofasapp.core.android.ktx.openSafely
import com.twofasapp.core.android.viewmodel.ProvidesViewModelStoreOwner
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.items.ItemImage
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.text.secretAnnotatedString
import com.twofasapp.core.design.foundation.text.secretString
import com.twofasapp.core.design.foundation.textfield.SecretFieldTrailingIcon
import com.twofasapp.core.design.foundation.textfield.passwordColorized
import com.twofasapp.core.design.theme.ButtonHeight
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.itemform.modals.securitytype.asTitle
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun ItemDetailsModal(
    item: Item,
    tags: List<Tag>,
    onDismissRequest: () -> Unit,
    onEditClick: () -> Unit = {},
    onCopySecretFieldToClipboard: (SecretField?) -> Unit = {},
) {
    ProvidesViewModelStoreOwner {
        ItemDetailsContent(
            item = item,
            tags = tags,
            onDismissRequest = onDismissRequest,
            onEditClick = onEditClick,
            onCopySecretFieldToClipboard = onCopySecretFieldToClipboard,
        )
    }
}

@Composable
private fun ItemDetailsContent(
    viewModel: ItemDetailsViewModel = koinViewModel(),
    item: Item,
    tags: List<Tag>,
    onDismissRequest: () -> Unit,
    onEditClick: () -> Unit = {},
    onCopySecretFieldToClipboard: (SecretField?) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        viewModel.init(item, tags)
    }

    LifecycleResumeEffect(Unit) {
        onPauseOrDispose {
            viewModel.clearDecryptedFields()
        }
    }

    Modal(onDismissRequest = onDismissRequest) {
        ModalContent(
            uiState = uiState,
            onEditClick = onEditClick,
            onToggleFieldVisibility = viewModel::toggleFieldVisibility,
            onCopySecretFieldToClipboard = onCopySecretFieldToClipboard,
            context = context,
            uriHandler = uriHandler,
        )
    }
}

@Composable
private fun ModalContent(
    uiState: ItemDetailsUiState,
    onEditClick: () -> Unit,
    onToggleFieldVisibility: (SecretFieldType) -> Unit,
    onCopySecretFieldToClipboard: (SecretField?) -> Unit,
    context: android.content.Context,
    uriHandler: androidx.compose.ui.platform.UriHandler,
) {
    val item = uiState.item
    val tags = uiState.tags

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MdtTheme.color.surfaceContainerLow)
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(bottom = ButtonHeight + 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ItemImage(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                item = item,
                size = 50.dp,
            )

            Text(
                text = item.content.name.ifEmpty { MdtLocale.strings.loginNoItemName },
                style = MdtTheme.typo.medium.lg.copy(lineHeight = 22.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                textAlign = TextAlign.Center,
            )

            ItemDetailsTags(
                item = item,
                tags = tags,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .clip(RoundedShape12)
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                item.content.let { content ->
                    when (content) {
                        is ItemContent.Unknown -> Unit
                        is ItemContent.Login -> {
                            if (content.username.isNullOrEmpty().not()) {
                                ItemDetailsEntry(
                                    title = MdtLocale.strings.loginUsername,
                                    subtitle = content.username.orEmpty(),
                                    actions = {
                                        IconButton(
                                            icon = MdtIcons.Copy,
                                            onClick = { context.copyToClipboard(content.username.orEmpty()) },
                                        )
                                    },
                                )
                            }

                            content.password?.let { password ->
                                val passwordDecrypted = uiState.decryptedFields[SecretFieldType.LoginPassword]

                                ItemDetailsEntry(
                                    title = MdtLocale.strings.loginPassword,
                                    subtitleAnnotated = passwordDecrypted?.let { passwordColorized(password = it) } ?: secretAnnotatedString(),
                                    actions = {
                                        SecretFieldTrailingIcon(
                                            visible = passwordDecrypted != null,
                                            onToggle = { onToggleFieldVisibility(SecretFieldType.LoginPassword) },
                                        )

                                        IconButton(
                                            icon = MdtIcons.Copy,
                                            onClick = { onCopySecretFieldToClipboard(content.password) },
                                        )
                                    },
                                )
                            }

                            content.uris.forEachIndexed { index, uri ->
                                if (uri.text.isNotEmpty()) {
                                    ItemDetailsEntry(
                                        title = if (content.uris.size > 1) "${MdtLocale.strings.loginUri} ${index + 1}" else MdtLocale.strings.loginUri,
                                        subtitle = uri.text,
                                        isCompact = true,
                                        maxLines = 3,
                                        actions = {
                                            IconButton(
                                                icon = MdtIcons.Open,
                                                onClick = { uriHandler.openSafely(uri.text, context) },
                                            )

                                            IconButton(
                                                icon = MdtIcons.Copy,
                                                onClick = { context.copyToClipboard(uri.text) },
                                            )
                                        },
                                    )
                                }
                            }

                            if (content.notes.isNullOrEmpty().not()) {
                                ItemDetailsEntry(
                                    title = MdtLocale.strings.loginNotes,
                                    subtitle = content.notes.orEmpty(),
                                    isCompact = true,
                                    actions = {
                                        IconButton(
                                            icon = MdtIcons.Copy,
                                            onClick = { context.copyToClipboard(content.notes.orEmpty()) },
                                        )
                                    },
                                )
                            }
                        }

                        is ItemContent.SecureNote -> {
                            content.text?.let {
                                val textDecrypted = uiState.decryptedFields[SecretFieldType.SecureNote]

                                ItemDetailsEntry(
                                    title = MdtLocale.strings.secureNoteText,
                                    subtitle = textDecrypted ?: secretString(),
                                    actions = {
                                        SecretFieldTrailingIcon(
                                            visible = textDecrypted != null,
                                            onToggle = { onToggleFieldVisibility(SecretFieldType.SecureNote) },
                                        )

                                        IconButton(
                                            icon = MdtIcons.Copy,
                                            onClick = { onCopySecretFieldToClipboard(content.text) },
                                        )
                                    },
                                )
                            }

                            if (content.additionalInfo.isNullOrEmpty().not()) {
                                ItemDetailsEntry(
                                    title = MdtLocale.strings.loginNotes,
                                    subtitle = content.additionalInfo.orEmpty(),
                                    isCompact = true,
                                    actions = {
                                        IconButton(
                                            icon = MdtIcons.Copy,
                                            onClick = { context.copyToClipboard(content.additionalInfo.orEmpty()) },
                                        )
                                    },
                                )
                            }
                        }

                        is ItemContent.PaymentCard -> {
                            if (content.cardHolder.isNullOrEmpty().not()) {
                                ItemDetailsEntry(
                                    title = MdtLocale.strings.cardHolderLabel,
                                    subtitle = content.cardHolder,
                                    actions = {
                                        IconButton(
                                            icon = MdtIcons.Copy,
                                            onClick = { context.copyToClipboard(content.cardHolder.orEmpty()) },
                                        )
                                    },
                                )
                            }

                            content.cardNumber?.let {
                                val textDecrypted = uiState.decryptedFields[SecretFieldType.PaymentCardNumber]

                                ItemDetailsEntry(
                                    title = MdtLocale.strings.cardNumberLabel,
                                    subtitle = textDecrypted ?: content.cardNumberMaskDisplayShort,
                                    actions = {
                                        SecretFieldTrailingIcon(
                                            visible = textDecrypted != null,
                                            onToggle = { onToggleFieldVisibility(SecretFieldType.PaymentCardNumber) },
                                        )

                                        IconButton(
                                            icon = MdtIcons.Copy,
                                            onClick = { onCopySecretFieldToClipboard(content.cardNumber) },
                                        )
                                    },
                                )
                            }

                            content.expirationDate?.let {
                                val textDecrypted = uiState.decryptedFields[SecretFieldType.PaymentCardExpiration]

                                ItemDetailsEntry(
                                    title = MdtLocale.strings.cardExpirationDateLabel,
                                    subtitle = textDecrypted ?: secretString(count = 5),
                                    actions = {
                                        SecretFieldTrailingIcon(
                                            visible = textDecrypted != null,
                                            onToggle = { onToggleFieldVisibility(SecretFieldType.PaymentCardExpiration) },
                                        )

                                        IconButton(
                                            icon = MdtIcons.Copy,
                                            onClick = { onCopySecretFieldToClipboard(content.expirationDate) },
                                        )
                                    },
                                )
                            }

                            content.securityCode?.let {
                                val textDecrypted = uiState.decryptedFields[SecretFieldType.PaymentCardSecureCode]

                                ItemDetailsEntry(
                                    title = MdtLocale.strings.cardSecurityCodeLabel,
                                    subtitle = textDecrypted ?: secretString(count = 3),
                                    actions = {
                                        SecretFieldTrailingIcon(
                                            visible = textDecrypted != null,
                                            onToggle = { onToggleFieldVisibility(SecretFieldType.PaymentCardSecureCode) },
                                        )

                                        IconButton(
                                            icon = MdtIcons.Copy,
                                            onClick = { onCopySecretFieldToClipboard(content.securityCode) },
                                        )
                                    },
                                )
                            }

                            if (content.notes.isNullOrEmpty().not()) {
                                ItemDetailsEntry(
                                    title = MdtLocale.strings.loginNotes,
                                    subtitle = content.notes.orEmpty(),
                                    isCompact = true,
                                    actions = {
                                        IconButton(
                                            icon = MdtIcons.Copy,
                                            onClick = { context.copyToClipboard(content.notes.orEmpty()) },
                                        )
                                    },
                                )
                            }
                        }
                    }

                    ItemDetailsEntry(
                        title = MdtLocale.strings.loginSecurityLevel,
                        subtitle = item.securityType.asTitle(),
                    )
                }
            }
        }

        Button(
            text = MdtLocale.strings.commonEdit,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            onClick = { onEditClick() },
        )
    }
}