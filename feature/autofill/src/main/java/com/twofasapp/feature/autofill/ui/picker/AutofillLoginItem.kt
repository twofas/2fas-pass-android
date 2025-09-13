/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.ui.picker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.feature.items.ItemEntry
import com.twofasapp.core.design.feature.items.LoginItemPreview
import com.twofasapp.core.design.foundation.dialog.ActionsAlignment
import com.twofasapp.core.design.foundation.dialog.InfoDialog
import com.twofasapp.core.design.foundation.preview.PreviewColumn
import com.twofasapp.core.locale.MdtLocale

@Composable
internal fun AutofillLoginItem(
    modifier: Modifier = Modifier,
    item: Item,
    query: String = "",
    suggested: Boolean,
    onFillAndRememberClick: (Item) -> Unit = {},
    onFillClick: (Item) -> Unit = {},
) {
    var showAutofillDialog by remember { mutableStateOf(false) }

    ItemEntry(
        modifier = modifier
            .clickable {
                if (suggested) {
                    onFillClick(item)
                } else {
                    showAutofillDialog = true
                }
            }
            .height(72.dp)
            .padding(horizontal = 16.dp),
        item = item,
        query = query,
    )

    if (showAutofillDialog) {
        InfoDialog(
            onDismissRequest = { showAutofillDialog = false },
            title = MdtLocale.strings.autofillLoginDialogTitle,
            bodyAnnotated = buildAnnotatedString {
                append(MdtLocale.strings.autofillLoginDialogBodyPrefix)
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(item.content.name.ifEmpty { MdtLocale.strings.loginNoItemName })
                }
                append(MdtLocale.strings.autofillLoginDialogBodySuffix)
            },
            icon = MdtIcons.Autofill,
            actionsAlignment = ActionsAlignment.Vertical,
            positive = MdtLocale.strings.autofillLoginDialogPositive,
            neutral = MdtLocale.strings.autofillLoginDialogNeutral,
            negative = MdtLocale.strings.commonCancel,
            onPositive = { onFillAndRememberClick(item) },
            onNeutral = { onFillClick(item) },
        )
    }
}

@Preview
@Composable
private fun Previews() {
    PreviewColumn {
        AutofillLoginItem(
            modifier = Modifier.fillMaxWidth(),
            item = LoginItemPreview,
            suggested = false,
        )
    }
}