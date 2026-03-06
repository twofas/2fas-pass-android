/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.itemdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.viewmodel.ProvidesViewModelStoreOwner
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.theme.ButtonHeight
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.home.ui.itemdetails.content.LoginContent
import com.twofasapp.feature.home.ui.itemdetails.content.PaymentCardContent
import com.twofasapp.feature.home.ui.itemdetails.content.SecureNoteContent
import com.twofasapp.feature.home.ui.itemdetails.content.WifiContent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun ItemDetailsModal(
    item: Item,
    tags: ImmutableList<Tag>,
    onDismissRequest: () -> Unit,
    onEditClick: () -> Unit = {},
) {
    Modal(
        onDismissRequest = onDismissRequest,
    ) {
        ProvidesViewModelStoreOwner {
            ModalContent(
                item = item,
                tags = tags,
                onEditClick = onEditClick,
            )
        }
    }
}

@Composable
private fun ModalContent(
    viewModel: ItemDetailsModalViewModel = koinViewModel(),
    item: Item,
    tags: ImmutableList<Tag>,
    onEditClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.initialize(item, tags)
    }

    LifecycleResumeEffect(Unit) {
        onPauseOrDispose { viewModel.clearDecryptedFields() }
    }

    Content(
        uiState = uiState,
        onEditClick = onEditClick,
    )
}

@Composable
private fun Content(
    viewModel: ItemDetailsModalViewModel = koinViewModel(),
    uiState: ItemDetailsModalUiState,
    onEditClick: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MdtTheme.color.surfaceContainerLow)
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = ButtonHeight + 16.dp)
                .verticalScroll(scrollState),
        ) {
            when (val content = uiState.item.content) {
                is ItemContent.Unknown -> Unit

                is ItemContent.Login -> LoginContent(
                    item = uiState.item,
                    tags = uiState.tags,
                    content = content,
                    decryptedFields = uiState.decryptedFields,
                    onToggleSecretField = viewModel::toggleSecretField,
                    onCopySecretField = viewModel::copySecretFieldToClipboard,
                )

                is ItemContent.SecureNote -> SecureNoteContent(
                    item = uiState.item,
                    tags = uiState.tags,
                    content = content,
                    decryptedFields = uiState.decryptedFields,
                    onToggleSecretField = viewModel::toggleSecretField,
                    onCopySecretField = viewModel::copySecretFieldToClipboard,
                )

                is ItemContent.PaymentCard -> PaymentCardContent(
                    item = uiState.item,
                    tags = uiState.tags,
                    content = content,
                    decryptedFields = uiState.decryptedFields,
                    onToggleSecretField = viewModel::toggleSecretField,
                    onCopySecretField = viewModel::copySecretFieldToClipboard,
                )

                is ItemContent.Wifi -> WifiContent(
                    item = uiState.item,
                    tags = uiState.tags,
                    content = content,
                    decryptedFields = uiState.decryptedFields,
                    onToggleSecretField = viewModel::toggleSecretField,
                    onCopySecretField = viewModel::copySecretFieldToClipboard,
                    onScrollToBottom = { scope.launch { scrollState.animateScrollTo(scrollState.maxValue) } },
                )

                is ItemContent.Passkey -> Unit//TODO passkey
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