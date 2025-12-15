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
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.ktx.formatDateTime
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.lazy.listItem

internal fun LazyListScope.timestampInfoItem(
    item: Item,
) {
    if (item.id.isNotEmpty()) {
        listItem(FormListItem.TimestampInfo) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem(),
                text = buildString {
                    append(" Created at: ")
                    append(item.createdAt.formatDateTime())
                    appendLine()
                    append(" Updated at: ")
                    append(item.updatedAt.formatDateTime())
                },
                style = MdtTheme.typo.bodySmall,
                color = MdtTheme.color.onSurface28,
            )
        }
    }
}