/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform.forms.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.foundation.layout.ZeroPadding
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.itemform.modals.tags.TagsPickerModal

@Composable
internal fun TagsPicker(
    modifier: Modifier = Modifier,
    tags: List<Tag>,
    selectedTagIds: List<String>,
    onOpened: () -> Unit = {},
    onConfirmTagsSelections: (List<String>) -> Unit = {},
) {
    var showTagsModal by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedShape12)
            .background(MdtTheme.color.surfaceContainer)
            .clickable {
                onOpened()
                showTagsModal = true
            }
            .padding(vertical = 8.dp)
            .padding(start = 12.dp, end = 4.dp),
    ) {
        OptionEntry(
            icon = MdtIcons.Tag,
            title = MdtLocale.strings.loginTagsHeader,
            subtitle = if (selectedTagIds.isEmpty()) {
                MdtLocale.strings.loginTagsDescription
            } else {
                tags.filter { selectedTagIds.contains(it.id) }.joinToString(", ") { it.name }
            },
            content = {
                Icon(
                    painter = MdtIcons.ChevronRight,
                    contentDescription = null,
                    tint = MdtTheme.color.onSurface,
                )
            },
            contentPadding = ZeroPadding,
        )
    }

    if (showTagsModal) {
        TagsPickerModal(
            tags = tags,
            selectedTagIds = selectedTagIds,
            forceEnableConfirmButton = false,
            onDismissRequest = { showTagsModal = false },
            onConfirmTagsSelections = onConfirmTagsSelections,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        TagsPicker(
            modifier = Modifier.fillMaxWidth(),
            tags = listOf(
                Tag.Empty.copy(id = "1", name = "Tag 1"),
                Tag.Empty.copy(id = "2", name = "Tag 2"),
                Tag.Empty.copy(id = "3", name = "Tag 3"),
            ),
            selectedTagIds = emptyList(),
        )
    }
}