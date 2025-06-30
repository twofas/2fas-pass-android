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
import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.feature.login.LoginEntry
import com.twofasapp.core.design.foundation.dialog.ActionsAlignment
import com.twofasapp.core.design.foundation.dialog.InfoDialog
import com.twofasapp.core.design.foundation.preview.PreviewColumn
import com.twofasapp.core.locale.MdtLocale

@Composable
internal fun AutofillLoginItem(
    modifier: Modifier = Modifier,
    login: Login,
    query: String = "",
    suggested: Boolean,
    onFillAndRememberClick: (Login) -> Unit = {},
    onFillClick: (Login) -> Unit = {},
) {
    var showAutofillDialog by remember { mutableStateOf(false) }

    LoginEntry(
        modifier = modifier
            .clickable {
                if (suggested) {
                    onFillClick(login)
                } else {
                    showAutofillDialog = true
                }
            }
            .height(72.dp)
            .padding(horizontal = 16.dp),
        login = login,
        query = query,
    )

    if (showAutofillDialog) {
        InfoDialog(
            onDismissRequest = { showAutofillDialog = false },
            title = MdtLocale.strings.autofillLoginDialogTitle,
            bodyAnnotated = buildAnnotatedString {
                append(MdtLocale.strings.autofillLoginDialogBodyPrefix)
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(login.name.ifEmpty { MdtLocale.strings.loginNoItemName })
                }
                append(MdtLocale.strings.autofillLoginDialogBodySuffix)
            },
            icon = MdtIcons.Autofill,
            actionsAlignment = ActionsAlignment.Vertical,
            positive = MdtLocale.strings.autofillLoginDialogPositive,
            neutral = MdtLocale.strings.autofillLoginDialogNeutral,
            negative = MdtLocale.strings.commonCancel,
            onPositive = { onFillAndRememberClick(login) },
            onNeutral = { onFillClick(login) },
        )
    }
}

@Preview
@Composable
private fun Previews() {
    PreviewColumn {
        AutofillLoginItem(
            modifier = Modifier.fillMaxWidth(),
            login = Login.Preview,
            suggested = false,
        )
    }
}