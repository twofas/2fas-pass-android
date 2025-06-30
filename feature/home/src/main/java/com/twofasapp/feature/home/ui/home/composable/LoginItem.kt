/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.home.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.android.ktx.copyToClipboard
import com.twofasapp.core.android.ktx.openSafely
import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.design.AppTheme
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.login.LoginEntry
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.dialog.ConfirmDialog
import com.twofasapp.core.design.foundation.menu.DropdownMenu
import com.twofasapp.core.design.foundation.menu.DropdownMenuItem
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.settings.domain.LoginClickAction
import com.twofasapp.feature.home.ui.home.modal.LoginModal

@Composable
internal fun LoginItem(
    modifier: Modifier = Modifier,
    login: Login,
    loginClickAction: LoginClickAction,
    query: String = "",
    onEditClick: (Login) -> Unit = {},
    onTrashConfirmed: (Login) -> Unit = {},
    onCopyPasswordToClipboard: (Login) -> Unit = {},
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var showDropdown by remember { mutableStateOf(false) }
    var showLoginModal by remember { mutableStateOf(false) }
    var showTrashDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .clickable {
                when (loginClickAction) {
                    LoginClickAction.View -> {
                        showLoginModal = true
                    }

                    LoginClickAction.Edit -> {
                        onEditClick(login)
                    }

                    LoginClickAction.CopyPassword -> {
                        onCopyPasswordToClipboard(login)
                    }

                    LoginClickAction.OpenUri -> {
                        uriHandler.openSafely(login.uris.firstOrNull()?.text, context)
                    }
                }
            }
            .height(72.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(16.dp))

        LoginEntry(
            modifier = Modifier.weight(1f),
            login = login,
            query = query,
        )

        DropdownMenu(
            visible = showDropdown,
            onDismissRequest = { showDropdown = false },
            anchor = {
                IconButton(
                    icon = MdtIcons.More,
                    iconTint = MdtTheme.color.outline,
                    onClick = { showDropdown = true },
                )
            },
            content = {
                DropdownMenuItem(
                    text = MdtLocale.strings.homeItemView,
                    icon = MdtIcons.Visibility,
                    onClick = {
                        showDropdown = false
                        showLoginModal = true
                    },
                )

                DropdownMenuItem(
                    text = MdtLocale.strings.homeItemEdit,
                    icon = MdtIcons.Edit,
                    onClick = {
                        showDropdown = false
                        onEditClick(login)
                    },
                )

                DropdownMenuItem(
                    text = MdtLocale.strings.homeItemCopyUsername,
                    icon = MdtIcons.User,
                    onClick = {
                        showDropdown = false
                        context.copyToClipboard(login.username.orEmpty())
                    },
                )

                DropdownMenuItem(
                    text = MdtLocale.strings.homeItemCopyPassword,
                    icon = MdtIcons.Key,
                    onClick = {
                        showDropdown = false
                        onCopyPasswordToClipboard(login)
                    },
                )

                DropdownMenuItem(
                    text = MdtLocale.strings.homeItemOpenUri,
                    icon = MdtIcons.Open,
                    onClick = {
                        showDropdown = false
                        uriHandler.openSafely(login.uris.firstOrNull()?.text, context)
                    },
                )

                DropdownMenuItem(
                    text = MdtLocale.strings.homeItemDelete,
                    icon = MdtIcons.Delete,
                    contentColor = MdtTheme.color.error,
                    onClick = {
                        showDropdown = false
                        showTrashDialog = true
                    },
                )
            },
        )

        Spacer(modifier = Modifier.width(4.dp))
    }

    if (showLoginModal) {
        LoginModal(
            login = login,
            onDismissRequest = {
                showLoginModal = false
            },
            onEditClick = {
                showLoginModal = false
                onEditClick(login)
            },
            onCopyPasswordToClipboard = onCopyPasswordToClipboard,
        )
    }

    if (showTrashDialog) {
        ConfirmDialog(
            onDismissRequest = { showTrashDialog = false },
            title = MdtLocale.strings.loginDeleteConfirmTitle,
            body = MdtLocale.strings.loginDeleteConfirmBody,
            icon = MdtIcons.Delete,
            onPositive = { onTrashConfirmed(login) },
        )
    }
}

@Preview
@Composable
private fun Previews() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PreviewTheme {
            LoginItem(
                modifier = Modifier.fillMaxWidth(),
                login = Login.Preview,
                loginClickAction = LoginClickAction.View,
            )
        }

        PreviewTheme(appTheme = AppTheme.Light) {
            LoginItem(
                modifier = Modifier.fillMaxWidth(),
                login = Login.Preview,
                loginClickAction = LoginClickAction.View,
            )
        }
    }
}