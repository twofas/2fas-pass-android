/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.home.modal.itemdetails

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.layout.ActionsRow

@Composable
internal fun ItemDetailsEntry(
    title: String,
    subtitle: String? = null,
    subtitleAnnotated: AnnotatedString? = null,
    isCompact: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MdtTheme.color.surfaceContainerHigh)
            .padding(start = 16.dp, end = 0.dp, top = 16.dp, bottom = 16.dp)
            .animateContentSize(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row {
                    Text(
                        text = title,
                        style = MdtTheme.typo.medium.sm,
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = if (isCompact) MdtTheme.typo.regular.base.copy(lineHeight = 18.sp) else MdtTheme.typo.regular.base,
                        color = MdtTheme.color.onSurfaceVariant,
                        maxLines = maxLines,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (subtitleAnnotated != null) {
                    Text(
                        text = subtitleAnnotated,
                        style = if (isCompact) MdtTheme.typo.regular.base.copy(lineHeight = 18.sp) else MdtTheme.typo.regular.base,
                        color = MdtTheme.color.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            ActionsRow(useHorizontalPadding = true) {
                actions()
            }
        }
    }
}