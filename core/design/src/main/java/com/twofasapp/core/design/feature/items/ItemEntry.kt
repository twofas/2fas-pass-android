/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.feature.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.ktx.splitAndMatch
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewAllThemesInRow
import com.twofasapp.core.locale.MdtLocale

@Composable
fun ItemEntry(
    modifier: Modifier = Modifier,
    item: Item,
    query: String = "",
) {
    val querySelectStyle = SpanStyle(MdtTheme.color.primary)
    val noItemName = MdtLocale.strings.loginNoItemName

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ItemImage(
            item = item,
        )

        Space(16.dp)

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = buildAnnotatedString {
                    if (item.content.name.isEmpty()) {
                        append(noItemName)
                    } else {
                        item.content.name.splitAndMatch(substring = query, ignoreCase = true).forEach { (textPart, matches) ->
                            if (matches) withStyle(querySelectStyle) { append(textPart) } else append(textPart)
                        }
                    }
                },
                style = MdtTheme.typo.bold.base,
                color = MdtTheme.color.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (item.content.subtitle.isNullOrEmpty().not()) {
                Text(
                    text = buildAnnotatedString {
                        item.content.subtitle.orEmpty().splitAndMatch(substring = query, ignoreCase = true).forEach { (textPart, matches) ->
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

        Space(4.dp)
    }
}

private val ItemContent.subtitle: String?
    get() = when (this) {
        is ItemContent.Unknown -> null
        is ItemContent.Login -> username
        is ItemContent.SecureNote -> null
        is ItemContent.PaymentCard -> cardholder
    }

@Preview
@Composable
private fun Preview() {
    PreviewAllThemesInRow {
        ItemEntry(
            modifier = Modifier.fillMaxWidth(),
            item = itemPreview(LoginItemContentPreview),
        )

        ItemEntry(
            modifier = Modifier.fillMaxWidth(),
            item = itemPreview(LoginItemContentPreview.copy(name = "Name Name")),
            query = "na",
        )
    }
}