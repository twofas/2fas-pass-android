/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.android.ktx.openSafely
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.design.AppTheme
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.items.ItemEntry
import com.twofasapp.core.design.feature.items.LoginItemPreview
import com.twofasapp.core.design.feature.items.SecureNoteItemPreview
import com.twofasapp.core.design.foundation.dialog.ConfirmDialog
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewColumn
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.settings.domain.LoginClickAction
import com.twofasapp.feature.home.ui.home.modal.ItemDetailsModal

@Composable
internal fun HomeItem(
    modifier: Modifier = Modifier,
    item: Item,
    tags: List<Tag>,
    loginClickAction: LoginClickAction,
    query: String = "",
    editMode: Boolean = false,
    selected: Boolean = false,
    onEditClick: (itemId: String, vaultId: String) -> Unit = { _, _ -> },
    onTrashConfirmed: (itemId: String) -> Unit = {},
    onCopySecretFieldToClipboard: (Item, SecretField?) -> Unit = { _, _ -> },
    onEnabledEditMode: () -> Unit = {},
    onToggleSelection: (itemId: String) -> Unit = {},
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var showDetailsModal by remember { mutableStateOf(false) }
    var showTrashDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .clip(RoundedShape12)
            .combinedClickable(
                hapticFeedbackEnabled = editMode.not(),
                onLongClick = {
                    if (editMode.not()) {
                        onEnabledEditMode()
                        onToggleSelection(item.id)
                    }
                },
                onClick = {
                    if (editMode.not()) {
                        when (loginClickAction) {
                            LoginClickAction.View -> {
                                showDetailsModal = true
                            }

                            LoginClickAction.Edit -> {
                                onEditClick(item.id, item.vaultId)
                            }

                            LoginClickAction.CopyPassword -> {
                                (item.content as? ItemContent.Login)?.password?.let { password ->
                                    onCopySecretFieldToClipboard(item, password)
                                }
                            }

                            LoginClickAction.OpenUri -> {
                                (item.content as? ItemContent.Login)?.uris?.let { uris ->
                                    uriHandler.openSafely(uris.firstOrNull()?.text, context)
                                }
                            }
                        }
                    } else {
                        onToggleSelection(item.id)
                    }
                },
            )
            .height(72.dp)
            .padding(vertical = if (selected) 0.5.dp else 0.dp)
            .background(if (selected) MdtTheme.color.surfaceContainerHighest.copy(alpha = 0.5f) else MdtTheme.color.transparent),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Space(16.dp)

        ItemEntry(
            modifier = Modifier.weight(1f),
            item = item,
            query = query,
        )

        if (editMode) {
            Icon(
                painter = if (selected) {
                    MdtIcons.CircleCheckFilled
                } else {
                    MdtIcons.CircleUncheck
                },
                tint = MdtTheme.color.primary,
                contentDescription = null,
                modifier = Modifier.padding(end = 12.dp),
            )
        } else {
            HomeItemDropdownMenu(
                item = item,
                onDetailsClick = { showDetailsModal = true },
                onEditClick = { onEditClick(item.id, item.vaultId) },
                onCopySecretFieldToClipboard = { secretField -> onCopySecretFieldToClipboard(item, secretField) },
                onTrashClick = { showTrashDialog = true },
            )
        }

        Space(4.dp)
    }

    if (showDetailsModal) {
        ItemDetailsModal(
            item = item,
            tags = tags,
            onDismissRequest = {
                showDetailsModal = false
            },
            onEditClick = {
                showDetailsModal = false
                onEditClick(item.id, item.vaultId)
            },
            onCopySecretFieldToClipboard = { secretField ->
                onCopySecretFieldToClipboard(item, secretField)
            },
        )
    }

    if (showTrashDialog) {
        ConfirmDialog(
            onDismissRequest = { showTrashDialog = false },
            title = MdtLocale.strings.loginDeleteConfirmTitle,
            body = MdtLocale.strings.loginDeleteConfirmBody,
            icon = MdtIcons.Delete,
            onPositive = { onTrashConfirmed(item.id) },
        )
    }
}

@Preview
@Composable
private fun PreviewDark() {
    val items = listOf(LoginItemPreview, LoginItemPreview, SecureNoteItemPreview)

    PreviewColumn(
        theme = AppTheme.Dark,
    ) {
        items.forEachIndexed { index, item ->
            HomeItem(
                modifier = Modifier.fillMaxWidth(),
                item = item,
                tags = emptyList(),
                loginClickAction = LoginClickAction.View,
                editMode = index == 1,
                selected = index == 1,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewLight() {
    val items = listOf(LoginItemPreview, LoginItemPreview, SecureNoteItemPreview)

    PreviewColumn(
        theme = AppTheme.Light,
    ) {
        items.forEachIndexed { index, item ->
            HomeItem(
                modifier = Modifier.fillMaxWidth(),
                item = item,
                tags = emptyList(),
                loginClickAction = LoginClickAction.View,
                editMode = index == 1,
                selected = index == 1,
            )
        }
    }
}