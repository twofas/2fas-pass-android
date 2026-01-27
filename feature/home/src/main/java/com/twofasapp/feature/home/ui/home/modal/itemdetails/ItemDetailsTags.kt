/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.home.modal.itemdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.preview.PreviewRow
import com.twofasapp.core.design.foundation.text.TextIcon

@Composable
internal fun ItemDetailsTags(
    item: Item,
    tags: List<Tag>,
    modifier: Modifier = Modifier,
) {
    if (tags.isEmpty()) return

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        tags.filter { item.tagIds.contains(it.id) }.forEach { tag ->
            TagPill(tag = tag)
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
internal fun TagPill(
    tag: Tag,
    modifier: Modifier = Modifier,
) {
    TextIcon(
        text = tag.name,
        leadingIcon = MdtIcons.Tag,
        leadingIconSize = 14.dp,
        leadingIconTint = MdtTheme.color.onSecondaryContainer,
        color = MdtTheme.color.onSecondaryContainer,
        style = MdtTheme.typo.labelSmall,
        modifier = modifier
            .clip(CircleShape)
            .background(MdtTheme.color.secondaryContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Preview
@Composable
private fun PreviewTagPill() {
    PreviewRow {
        TagPill(
            tag = Tag.Empty.copy(name = "Personal"),
        )

        TagPill(
            tag = Tag.Empty.copy(name = "Work"),
        )
    }
}