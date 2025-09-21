/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform.forms.login.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.domain.ItemUri
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.layout.ActionsRow
import com.twofasapp.core.design.foundation.preview.PreviewColumn
import com.twofasapp.core.design.foundation.textfield.TextField
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.itemform.modals.urisettings.UriSettingsModal

@Composable
internal fun UriField(
    modifier: Modifier = Modifier,
    index: Int,
    itemUri: ItemUri,
    totalCount: Int,
    onUriChange: (Int, ItemUri) -> Unit = { _, _ -> },
    onDeleteUri: (Int) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    var showUriSettingsModal by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextField(
            value = itemUri.text,
            onValueChange = { onUriChange(index, itemUri.copy(text = it)) },
            labelText = if (totalCount == 1) {
                MdtLocale.strings.loginUri
            } else {
                "${MdtLocale.strings.loginUri} ${index + 1}"
            },
            modifier = Modifier.weight(1f),
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            trailingIcon = {
                ActionsRow(
                    modifier = Modifier.padding(end = 4.dp),
                ) {
                    IconButton(
                        icon = MdtIcons.Settings,
                        onClick = {
                            focusManager.clearFocus()
                            showUriSettingsModal = true
                        },
                    )

                    if (totalCount > 1) {
                        IconButton(
                            icon = MdtIcons.Delete,
                            onClick = { onDeleteUri(index) },
                        )
                    }
                }
            },
        )
    }

    if (showUriSettingsModal) {
        UriSettingsModal(
            onDismissRequest = { showUriSettingsModal = false },
            itemUri = itemUri,
            onSelectMatcher = { onUriChange(index, itemUri.copy(matcher = it)) },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewColumn {
        UriField(
            modifier = Modifier.fillMaxWidth(),
            index = 0,
            itemUri = ItemUri("https://google.com"),
            totalCount = 2,
        )

        UriField(
            modifier = Modifier.fillMaxWidth(),
            index = 1,
            itemUri = ItemUri("https://google.com"),
            totalCount = 2,
        )
    }
}