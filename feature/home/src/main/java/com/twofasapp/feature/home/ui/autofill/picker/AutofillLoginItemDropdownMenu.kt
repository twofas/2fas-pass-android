/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.autofill.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.menu.DropdownMenu
import com.twofasapp.core.design.foundation.menu.DropdownMenuItem
import com.twofasapp.core.locale.MdtLocale

@Composable
internal fun AutofillLoginItemDropdownMenu(
    item: Item,
    onEditClick: () -> Unit = {},
    onCopyUsernameClick: () -> Unit = {},
    onCopyPasswordClick: () -> Unit = {},
) {
    val content = item.content as? ItemContent.Login ?: return
    var showDropdown by remember { mutableStateOf(false) }

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
                text = MdtLocale.strings.homeItemEdit,
                leadingIcon = MdtIcons.Edit,
                onClick = {
                    showDropdown = false
                    onEditClick()
                },
            )

            content.username.takeIf { it.isNullOrEmpty().not() }?.let {
                DropdownMenuItem(
                    text = MdtLocale.strings.homeItemCopyUsername,
                    leadingIcon = MdtIcons.User,
                    onClick = {
                        showDropdown = false
                        onCopyUsernameClick()
                    },
                )
            }

            content.password?.let {
                DropdownMenuItem(
                    text = MdtLocale.strings.homeItemCopyPassword,
                    leadingIcon = MdtIcons.Key,
                    onClick = {
                        showDropdown = false
                        onCopyPasswordClick()
                    },
                )
            }
        },
    )
}