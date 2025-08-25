/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.home.modal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.locale.MdtLocale

@Composable
internal fun ListOptionsModal(
    selectedTag: Tag? = null,
    onDismissRequest: () -> Unit,
    onSortClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onClearClick: () -> Unit = {},
) {
    Modal(
        onDismissRequest = onDismissRequest,
        headerText = MdtLocale.strings.homeListOptionsModalTitle,
    ) { dismissAction ->
        Content(
            selectedTag = selectedTag,
            onSortClick = { dismissAction { onSortClick() } },
            onFilterClick = { dismissAction { onFilterClick() } },
            onClearClick = { dismissAction { onClearClick() } },
        )
    }
}

@Composable
private fun Content(
    selectedTag: Tag? = null,
    onSortClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onClearClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        OptionEntry(
            title = "Sort by",
            icon = MdtIcons.Sort,
            onClick = { onSortClick() },
        )

        OptionEntry(
            title = if (selectedTag == null) {
                "Filter"
            } else {
                "Filter (${selectedTag.name})"
            },
            icon = MdtIcons.FilterAlt,
            onClick = { onFilterClick() },
            content = {
                if (selectedTag != null) {
                    Icon(
                        painter = MdtIcons.CircleFilled,
                        contentDescription = null,
                        tint = MdtTheme.color.notice,
                        modifier = Modifier.size(12.dp),
                    )
                }
            },
        )

        if (selectedTag != null) {
            OptionEntry(
                title = "Clear filters",
                icon = MdtIcons.Close,
                titleColor = MdtTheme.color.error,
                iconTint = MdtTheme.color.error,
                onClick = { onClearClick() },
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content()
    }
}

@Preview
@Composable
private fun PreviewSelectedTag() {
    PreviewTheme {
        Content(
            selectedTag = Tag.Empty.copy(name = "Work"),
        )
    }
}