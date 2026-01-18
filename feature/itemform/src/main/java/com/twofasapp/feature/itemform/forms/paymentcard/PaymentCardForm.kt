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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.clearTextOrNull
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.cardNumberGrouping
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.items.ItemImage
import com.twofasapp.core.design.foundation.layout.ActionsRow
import com.twofasapp.core.design.foundation.lazy.listItem
import com.twofasapp.core.design.foundation.textfield.PaymentCard
import com.twofasapp.core.design.foundation.textfield.PaymentCardExpirationDate
import com.twofasapp.core.design.foundation.textfield.SecretField
import com.twofasapp.core.design.foundation.textfield.SecretFieldTrailingIcon
import com.twofasapp.core.design.foundation.textfield.TextField
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.main.mapper.PaymentCardValidator
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
    var cardNumberFocused by remember { mutableStateOf(false) }
    var cvvVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

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
                    trailingIcon = {
                        ItemImage(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedShape12),
                            item = uiState.item,
                            size = 42.dp,
                        )
                    },
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
                val cardNumberValue = if (cardNumberFocused) {
                    uiState.itemContent.cardNumber?.clearTextOrNull.orEmpty()
                } else {
                    uiState.itemContent.cardNumberMaskDisplay
                }

                val maxLength = PaymentCardValidator.maxCardNumberLength(uiState.itemContent.cardIssuer)

                val isCardNumberValid = uiState.itemContent.cardNumber.clearTextOrNull?.let {
                    PaymentCardValidator.validateCardNumber(
                        value = it,
                        issuer = uiState.itemContent.cardIssuer,
                    )
                } ?: true

                val grouping = uiState.itemContent.cardIssuer.cardNumberGrouping()

                TextField(
                    value = cardNumberValue,
                    onValueChange = {
                        if (it.isDigitsOnly() && (it.length <= maxLength || it.length < cardNumberValue.length)) {
                            onCardNumberChange(it.trim())
                        }
                    },
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                    labelText = strings.creditCardNumber,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .onFocusChanged { cardNumberFocused = it.isFocused },
                    singleLine = true,
                    maxLines = 1,
                    visualTransformation = if (cardNumberFocused) VisualTransformation.PaymentCard(grouping) else VisualTransformation.None,
                    isError = isCardNumberValid.not(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                )
            }

            listItem(FormListItem.Field("ExpirationAndSecurityCode")) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextField(
                        value = uiState.itemContent.expirationDate?.clearTextOrNull.orEmpty().replace("/", ""),
                        onValueChange = {
                            if (it.length <= 4 && it.isDigitsOnly()) {
                                onExpirationDateChange(it.trim())
                            }
                        },
                        labelText = strings.creditCardExpiration,
                        placeholderText = "MM / YY",
                        modifier = Modifier.weight(0.6f),
                        singleLine = true,
                        maxLines = 1,
                        isError = uiState.itemContent.expirationDate.clearTextOrNull?.let {
                            PaymentCardValidator.validateExpirationDate(
                                value = it,
                            ).not()
                        } ?: false,
                        visualTransformation = VisualTransformation.PaymentCardExpirationDate,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                        ),
                    )

                    val maxCvvLength = PaymentCardValidator.maxSecurityCodeLength(uiState.itemContent.cardIssuer)

                    TextField(
                        value = uiState.itemContent.securityCode?.clearTextOrNull.orEmpty(),
                        onValueChange = {
                            if (it.length <= maxCvvLength && it.isDigitsOnly()) {
                                onSecurityCodeChange(it.trim())
                            }
                        },
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                        labelText = strings.creditCardCvv,
                        modifier = Modifier.weight(0.4f),
                        singleLine = true,
                        maxLines = 1,
                        isError = uiState.itemContent.securityCode.clearTextOrNull?.let {
                            PaymentCardValidator.validateSecurityCode(
                                value = uiState.itemContent.securityCode?.clearTextOrNull.orEmpty(),
                                issuer = uiState.itemContent.cardIssuer,
                            ).not()
                        } ?: false,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        visualTransformation = VisualTransformation.SecretField(cvvVisible),
                        trailingIcon = {
                            ActionsRow(
                                useHorizontalPadding = true,
                            ) {
                                SecretFieldTrailingIcon(
                                    visible = cvvVisible,
                                    onToggle = { cvvVisible = cvvVisible.not() },
                                )
                            }
                        },
                    )
                }
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