/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform.forms.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.twofasapp.core.design.foundation.lazy.listItem
import com.twofasapp.core.design.foundation.textfield.TextField
import com.twofasapp.core.locale.MdtLocale

internal fun LazyListScope.noteItem(
    notes: String?,
    label: String? = null,
    onNotesChange: (String) -> Unit,
) {
    listItem(FormListItem.Field("Notes")) {
        TextField(
            value = notes.orEmpty(),
            onValueChange = { onNotesChange(it) },
            labelText = label ?: MdtLocale.strings.loginNotes,
            modifier = Modifier
                .fillMaxWidth()
                .animateItem(),
            minLines = 3,
            maxLines = 3,
            supportingText = if (notes.orEmpty().length > 2048) {
                MdtLocale.strings.noteItemLengthError.format(2048)
            } else {
                null
            },
            isError = notes.orEmpty().length > 2048,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
        )
    }
}