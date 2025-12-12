/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform.forms.paymentcard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.lazy.listItem
import com.twofasapp.core.design.foundation.textfield.TextField
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.itemform.ItemFormListener
import com.twofasapp.feature.itemform.ItemFormProperties
import com.twofasapp.feature.itemform.ItemFormUiState
import com.twofasapp.feature.itemform.forms.common.FormListItem
import com.twofasapp.feature.itemform.forms.common.ItemContentFormContainer
import com.twofasapp.feature.itemform.forms.common.noteItem
import com.twofasapp.feature.itemform.forms.common.securityTypePickerItem
import com.twofasapp.feature.itemform.forms.common.tagsPickerItem
import com.twofasapp.feature.itemform.forms.common.timestampInfoItem
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun PaymentCardForm(
    modifier: Modifier = Modifier,
    viewModel: PaymentCardFormViewModel = koinViewModel(),
    initialItem: Item,
    containerColor: Color = MdtTheme.color.background,
    properties: ItemFormProperties,
    listener: ItemFormListener,
) {
    ItemContentFormContainer(
        viewModel = viewModel,
        initialItem = initialItem,
        properties = properties,
        listener = listener,
    ) { uiState ->
        Content(
            modifier = modifier,
            uiState = uiState,
            containerColor = containerColor,
            onNameChange = { viewModel.updateName(it) },
            onCardHolderChange = { viewModel.updateCardHolder(it) },
            onCardNumberChange = { viewModel.updateCardNumber(it) },
            onExpirationDateChange = { viewModel.updateExpirationDate(it) },
            onSecurityCodeChange = { viewModel.updateSecurityCode(it) },
            onNotesChange = { viewModel.updateNotes(it) },
            onSecurityTypeChange = { viewModel.updateSecurityType(it) },
            onTagsChange = { viewModel.updateTags(it) },
        )
    }
}

@Composable
private fun Content(
    modifier: Modifier,
    uiState: ItemFormUiState<ItemContent.PaymentCard>,
    containerColor: Color,
    onNameChange: (String) -> Unit = {},
    onCardHolderChange: (String) -> Unit = {},
    onCardNumberChange: (String) -> Unit = {},
    onExpirationDateChange: (String) -> Unit = {},
    onSecurityCodeChange: (String) -> Unit = {},
    onNotesChange: (String) -> Unit = {},
    onSecurityTypeChange: (SecurityType) -> Unit = {},
    onTagsChange: (List<String>) -> Unit = {},
) {
    if (uiState.itemContent == null) return

    val strings = MdtLocale.strings

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .background(containerColor),
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = ScreenPadding, top = 8.dp),
        ) {
            listItem(FormListItem.Field("Name")) {
                TextField(
                    value = uiState.itemContent.name,
                    onValueChange = onNameChange,
                    labelText = strings.secureNoteName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next,
                    ),
                )
            }

            listItem(FormListItem.Field("CardHolder")) {
                TextField(
                    value = uiState.itemContent.cardHolder.orEmpty(),
                    onValueChange = onCardHolderChange,
                    labelText = strings.creditCardCardholder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                    ),
                )
            }

            listItem(FormListItem.Field("CardNumber")) {
            }

            listItem(FormListItem.Field("ExpirationAndSecurityCode")) {
            }

            securityTypePickerItem(
                item = uiState.item,
                onSecurityTypeChange = onSecurityTypeChange,
            )

            tagsPickerItem(
                item = uiState.item,
                tags = uiState.tags,
                onTagsChange = onTagsChange,
            )

            noteItem(
                notes = uiState.itemContent.notes,
                onNotesChange = onNotesChange,
            )

            timestampInfoItem(item = uiState.item)
        }
    }
}