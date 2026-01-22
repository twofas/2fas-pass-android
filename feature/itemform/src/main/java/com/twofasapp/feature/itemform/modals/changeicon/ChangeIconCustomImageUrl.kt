/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform.modals.changeicon

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.feature.settings.OptionHeader
import com.twofasapp.core.design.foundation.preview.PreviewColumn
import com.twofasapp.core.design.foundation.textfield.TextField
import com.twofasapp.core.locale.MdtLocale

@Composable
internal fun ChangeIconCustomImageUrl(
    imageUrl: String?,
    onUrlChange: (String?) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    var customUrl by remember { mutableStateOf(imageUrl) }
    val strings = MdtLocale.strings

    OptionHeader(text = strings.changeIconCustomImageHeader)

    TextField(
        value = customUrl.orEmpty(),
        onValueChange = {
            customUrl = it
            onUrlChange(it)
        },
        placeholderText = strings.changeIconCustomImagePlaceholder,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Uri),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .onFocusChanged {
                if (it.hasFocus) {
                    onUrlChange(customUrl)
                }
            },
    )
}

@Preview
@Composable
private fun Preview() {
    PreviewColumn {
        ChangeIconCustomImageUrl(
            imageUrl = "",
        )
    }
}