/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.home.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonStyle
import com.twofasapp.core.design.foundation.checked.CheckIcon
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.locale.MdtLocale

@Composable
internal fun FilterModal(
    onDismissRequest: () -> Unit,
    tags: List<Tag> = emptyList(),
    selectedTag: Tag? = null,
    onToggle: (Tag) -> Unit = {},
    onManageTagsClick: () -> Unit = {},
) {
    Modal(
        onDismissRequest = onDismissRequest,
        headerText = "Filter",
    ) { dismissAction ->
        Content(
            tags = tags,
            selectedTag = selectedTag,
            onToggle = { dismissAction { onToggle(it) } },
            onManageTagsClick = { dismissAction { onManageTagsClick() } },
        )
    }
}

@Composable
private fun Content(
    tags: List<Tag> = emptyList(),
    selectedTag: Tag? = null,
    onToggle: (Tag) -> Unit = {},
    onManageTagsClick: () -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (tags.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = MdtLocale.strings.tagsEmptyList,
                        style = MdtTheme.typo.bodyLarge,
                        color = MdtTheme.color.onSurface,
                    )

                    Space(16.dp)

                    Button(
                        text = MdtLocale.strings.settingsEntryManageTags,
                        onClick = onManageTagsClick,
                        leadingIcon = MdtIcons.Tag,
                        style = ButtonStyle.Text,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MdtTheme.color.surfaceContainerHigh),
                    )
                }
            }
        }

        tags.forEach { tag ->
            item("Tag:${tag.id}", "Tag") {
                OptionEntry(
                    title = "${tag.name} (${tag.assignedItemsCount})",
                    titleColor = if (selectedTag?.id == tag.id) MdtTheme.color.primary else MdtTheme.color.onSurface,
                    icon = MdtIcons.Tag,
                    onClick = { onToggle(tag) },
                    content = { CheckIcon(checked = selectedTag?.id == tag.id) },
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            tags = List(3) { Tag.Empty.copy(id = "Tag $it", name = "Tag $it") },
            selectedTag = Tag.Empty.copy(id = "Tag 2", name = "Tag 2"),
        )
    }
}

@Preview
@Composable
private fun PreviewEmpty() {
    PreviewTheme {
        Content(
            tags = emptyList(),
            selectedTag = Tag.Empty.copy(name = "Tag 2"),
        )
    }
}