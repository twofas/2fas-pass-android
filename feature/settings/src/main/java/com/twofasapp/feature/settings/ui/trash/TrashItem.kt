/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.trash

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.design.AppTheme
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.items.ItemEntry
import com.twofasapp.core.design.feature.items.LoginItemPreview
import com.twofasapp.core.design.feature.items.SecureNoteItemPreview
import com.twofasapp.core.design.foundation.checked.CheckIcon
import com.twofasapp.core.design.foundation.preview.PreviewColumn

@Composable
internal fun TrashItem(
    modifier: Modifier = Modifier,
    item: Item,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit = {},
) {
    Row(
        modifier = modifier
            .clickable { onCheckedChange.invoke(checked.not()) }
            .background(if (checked) MdtTheme.color.surfaceContainer else MdtTheme.color.background)
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ItemEntry(
            modifier = Modifier.weight(1f),
            item = item,
        )

        CheckIcon(checked = checked)
    }
}

@Preview
@Composable
private fun PreviewDark() {
    PreviewColumn {
        TrashItem(
            modifier = Modifier.fillMaxWidth(),
            item = LoginItemPreview,
            checked = false,
        )

        TrashItem(
            modifier = Modifier.fillMaxWidth(),
            item = SecureNoteItemPreview,
            checked = true,
        )
    }
}

@Preview
@Composable
private fun PreviewLight() {
    PreviewColumn(theme = AppTheme.Light) {
        TrashItem(
            modifier = Modifier.fillMaxWidth(),
            item = LoginItemPreview,
            checked = false,
        )

        TrashItem(
            modifier = Modifier.fillMaxWidth(),
            item = SecureNoteItemPreview,
            checked = true,
        )
    }
}