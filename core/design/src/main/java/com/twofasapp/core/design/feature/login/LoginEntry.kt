/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.feature.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.common.ktx.splitAndMatch
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.preview.PreviewAllThemesInRow
import com.twofasapp.core.locale.MdtLocale

@Composable
fun LoginEntry(
    modifier: Modifier = Modifier,
    login: Login,
    query: String = "",
) {
    val querySelectStyle = SpanStyle(MdtTheme.color.primary)
    val noItemName = MdtLocale.strings.loginNoItemName

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LoginImage(
            iconType = login.iconType,
            iconUrl = login.iconUrl,
            labelText = login.labelText ?: login.defaultLabelText,
            labelColor = login.labelColor,
            customImageUrl = login.customImageUrl,
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = buildAnnotatedString {
                    if (login.name.isEmpty()) {
                        append(noItemName)
                    } else {
                        login.name.splitAndMatch(substring = query, ignoreCase = true).forEach { (textPart, matches) ->
                            if (matches) withStyle(querySelectStyle) { append(textPart) } else append(textPart)
                        }
                    }
                },
                style = MdtTheme.typo.bold.base,
                color = MdtTheme.color.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (login.username.isNullOrEmpty().not()) {
                Text(
                    text = buildAnnotatedString {
                        login.username.orEmpty().splitAndMatch(substring = query, ignoreCase = true).forEach { (textPart, matches) ->
                            if (matches) withStyle(SpanStyle(MdtTheme.color.primary)) { append(textPart) } else append(textPart)
                        }
                    },
                    style = MdtTheme.typo.regular.sm,
                    color = MdtTheme.color.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewAllThemesInRow {
        LoginEntry(
            modifier = Modifier.fillMaxWidth(),
            login = Login.Preview,
        )

        LoginEntry(
            modifier = Modifier.fillMaxWidth(),
            login = Login.Preview.copy(name = "Name Name"),
            query = "na",
        )
    }
}