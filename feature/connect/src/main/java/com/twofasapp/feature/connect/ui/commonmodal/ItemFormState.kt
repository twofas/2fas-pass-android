/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.connect.ui.commonmodal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonStyle
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.connect.ui.requestmodal.RequestState
import com.twofasapp.feature.itemform.ItemForm
import com.twofasapp.feature.itemform.ItemFormListener
import com.twofasapp.feature.itemform.ItemFormProperties

@Composable
internal fun ItemFormState(
    itemFormState: RequestState.FullSize.ItemForm,
) {
    var itemState by remember { mutableStateOf(itemFormState.item) }
    var isValid by remember { mutableStateOf(false) }
    val strings = MdtLocale.strings

    BackHandler {
        itemFormState.onCancel()
    }

    Column {
        TopAppBar(
            title = when {
                itemFormState.item.id.isBlank() -> {
                    when (itemFormState.item.contentType) {
                        ItemContentType.Login -> strings.loginAddTitle
                        ItemContentType.SecureNote -> strings.secureNoteAddTitle
                        ItemContentType.PaymentCard -> strings.itemAddTitle
                        is ItemContentType.Unknown -> strings.itemAddTitle
                    }
                }

                else -> {
                    when (itemFormState.item.contentType) {
                        ItemContentType.Login -> strings.loginEditTitle
                        ItemContentType.SecureNote -> strings.secureNoteEditTitle
                        ItemContentType.PaymentCard -> strings.itemEditTitle
                        is ItemContentType.Unknown -> strings.itemEditTitle
                    }
                }
            },
            onBackClick = { itemFormState.onCancel() },
            containerColor = MdtTheme.color.surfaceContainerLow,
            actions = {
                Button(
                    text = MdtLocale.strings.commonSave,
                    style = ButtonStyle.Text,
                    onClick = { itemFormState.onSaveClick(itemState) },
                    enabled = isValid,
                )
            },
        )

        ItemForm(
            modifier = Modifier.fillMaxSize(),
            initialItem = itemFormState.item,
            containerColor = MdtTheme.color.surfaceContainerLow,
            properties = ItemFormProperties.Default.copy(
                shouldConfirmUnsavedChanges = false,
            ),
            listener = object : ItemFormListener {
                override fun onItemUpdated(item: Item) {
                    itemState = item
                }

                override fun onIsValidUpdated(valid: Boolean) {
                    isValid = valid
                }

                override fun onCloseWithoutSaving() {
                    itemFormState.onCancel()
                }
            },
        )
    }
}