/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.itemdetails

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.layout.ActionsRow
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.home.ui.itemdetails.content.LoginContent
import com.twofasapp.feature.home.ui.itemdetails.content.PaymentCardContent
import com.twofasapp.feature.home.ui.itemdetails.content.SecureNoteContent
import com.twofasapp.feature.home.ui.itemdetails.content.WifiContent
import com.twofasapp.feature.share.ui.ShareItemModal
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun ItemDetailsScreen(
    viewModel: ItemDetailsViewModel = koinViewModel(),
    openEditItem: (itemId: String, vaultId: String, itemContentType: ItemContentType) -> Unit,
    close: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        onPauseOrDispose { viewModel.clearDecryptedFields() }
    }

    Content(
        uiState = uiState,
        onToggleSecretField = viewModel::toggleSecretField,
        onCopySecretField = viewModel::copySecretFieldToClipboard,
        onEditClick = {
            openEditItem(uiState.item.id, uiState.item.vaultId, uiState.item.contentType)
        },
        onBackClick = close,
    )
}

@Composable
private fun Content(
    uiState: ItemDetailsUiState,
    onToggleSecretField: (SecretFieldType, SecretField?) -> Unit = { _, _ -> },
    onCopySecretField: (SecretField?, (String) -> Unit) -> Unit = { _, _ -> },
    onEditClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var showShareModal by remember { mutableStateOf(false) }

    if (showShareModal) {
        ShareItemModal(
            item = uiState.item,
            onDismissRequest = { showShareModal = false },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = MdtLocale.strings.itemDetailsTitle,
                onBackClick = onBackClick,
                actions = {
                    ActionsRow(
                        useHorizontalPadding = true,
                        spacing = 8.dp,
                    ) {
                        IconButton(
                            modifier = Modifier.testTag("itemDetailsShareButton"),
                            icon = MdtIcons.Share,
                            onClick = { showShareModal = true },
                        )

                        IconButton(
                            modifier = Modifier.testTag("itemDetailsEditButton"),
                            icon = MdtIcons.Edit,
                            onClick = { onEditClick() },
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
        ) {
            Space(16.dp)

            when (val content = uiState.item.content) {
                is ItemContent.Unknown -> Unit

                is ItemContent.Login -> LoginContent(
                    item = uiState.item,
                    tags = uiState.tags,
                    content = content,
                    decryptedFields = uiState.decryptedFields,
                    onToggleSecretField = onToggleSecretField,
                    onCopySecretField = onCopySecretField,
                )

                is ItemContent.SecureNote -> SecureNoteContent(
                    item = uiState.item,
                    tags = uiState.tags,
                    content = content,
                    decryptedFields = uiState.decryptedFields,
                    onToggleSecretField = onToggleSecretField,
                    onCopySecretField = onCopySecretField,
                )

                is ItemContent.PaymentCard -> PaymentCardContent(
                    item = uiState.item,
                    tags = uiState.tags,
                    content = content,
                    decryptedFields = uiState.decryptedFields,
                    onToggleSecretField = onToggleSecretField,
                    onCopySecretField = onCopySecretField,
                )

                is ItemContent.Wifi -> WifiContent(
                    item = uiState.item,
                    tags = uiState.tags,
                    content = content,
                    decryptedFields = uiState.decryptedFields,
                    onToggleSecretField = onToggleSecretField,
                    onCopySecretField = onCopySecretField,
                    onScrollToBottom = { scope.launch { scrollState.animateScrollTo(scrollState.maxValue) } },
                )
            }

            Space(16.dp)
        }
    }
}