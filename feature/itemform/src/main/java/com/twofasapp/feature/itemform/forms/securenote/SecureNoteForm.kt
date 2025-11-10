/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform.forms.securenote

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.domain.SecretField
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
import com.twofasapp.feature.itemform.forms.common.securityTypePickerItem
import com.twofasapp.feature.itemform.forms.common.tagsPickerItem
import com.twofasapp.feature.itemform.forms.common.timestampInfoItem
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun SecureNoteForm(
    modifier: Modifier = Modifier,
    viewModel: SecureNoteFormViewModel = koinViewModel(),
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
            onTextChange = { viewModel.updateText(it) },
            onSecurityTypeChange = { viewModel.updateSecurityType(it) },
            onTagsChange = { viewModel.updateTags(it) },
        )
    }
}

@Composable
private fun Content(
    modifier: Modifier,
    uiState: ItemFormUiState<ItemContent.SecureNote>,
    containerColor: Color,
    onNameChange: (String) -> Unit = {},
    onTextChange: (String) -> Unit = {},
    onSecurityTypeChange: (SecurityType) -> Unit = {},
    onTagsChange: (List<String>) -> Unit = {},
) {
    if (uiState.itemContent == null) return

    val strings = MdtLocale.strings
    var textFieldValue by remember { mutableStateOf(TextFieldValue(text = "")) }

    LaunchedEffect(uiState.item.id) {
        textFieldValue = textFieldValue.copy(text = (uiState.itemContent.text as? SecretField.ClearText)?.value.orEmpty())
    }

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

            listItem(FormListItem.Field("Text")) {
                TextField(
                    value = textFieldValue,
                    onValueChange = {
                        onTextChange(it.text)
                        textFieldValue = it
                    },
                    labelText = strings.secureNoteText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    minLines = 12,
                    maxLines = 12,
                    supportingText = if (textFieldValue.text.length > ItemContent.SecureNote.Limit) "Notes can not be longer than ${ItemContent.SecureNote.Limit} characters" else null,
                    isError = textFieldValue.text.length > ItemContent.SecureNote.Limit,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Default),
                )
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

            timestampInfoItem(item = uiState.item)
        }
    }
}