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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.domain.SecurityItem
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.design.LocalDarkMode
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.feature.tags.iconTint
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonStyle
import com.twofasapp.core.design.foundation.checked.CheckIcon
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.itemform.modals.securitytype.asIcon
import com.twofasapp.feature.itemform.modals.securitytype.asTitle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

@Composable
internal fun FilterModal(
    onDismissRequest: () -> Unit,
    tags: List<Tag> = emptyList(),
    securityItems: ImmutableList<SecurityItem> = persistentListOf(),
    selectedTag: Tag? = null,
    selectedSecurityItem: SecurityItem?,
    onToggleTag: (Tag) -> Unit = {},
    onToggleSecurityItem: (SecurityItem) -> Unit = {},
    onManageTagsClick: () -> Unit = {},
) {
    Modal(
        onDismissRequest = onDismissRequest,
        headerText = MdtLocale.strings.loginFilterModalTag,
    ) { dismissAction ->
        Content(
            selectedSecurityItem = selectedSecurityItem,
            tags = tags,
            securityItems = securityItems,
            selectedTag = selectedTag,
            onToggleTag = { dismissAction { onToggleTag(it) } },
            onManageTagsClick = { dismissAction { onManageTagsClick() } },
            onToggleSecurityItem = { dismissAction { onToggleSecurityItem(it) } }
        )
    }
}

@Composable
private fun Content(
    tags: List<Tag> = emptyList(),
    securityItems: ImmutableList<SecurityItem> = persistentListOf(),
    selectedTag: Tag? = null,
    selectedSecurityItem: SecurityItem? = null,
    onToggleTag: (Tag) -> Unit = {},
    onToggleSecurityItem: (SecurityItem) -> Unit = {},
    onManageTagsClick: () -> Unit = {},
) {
    val strings = MdtLocale.strings
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
    ) {
        securityItems(
            securityTypes = securityItems,
            selectedSecurityItem = selectedSecurityItem,
            onClick = onToggleSecurityItem
        )
        divider()
        if (tags.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = strings.tagsEmptyList,
                        style = MdtTheme.typo.bodyLarge,
                        color = MdtTheme.color.onSurface,
                    )

                    Space(16.dp)

                    Button(
                        text = strings.settingsEntryManageTags,
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
                    title = strings.homeFilterTagWithCount.format(tag.name, tag.assignedItemsCount),
                    titleColor = MdtTheme.color.onSurface,
                    icon = MdtIcons.TagFilled,
                    iconTint = tag.iconTint(),
                    onClick = { onToggleTag(tag) },
                    content = { CheckIcon(checked = selectedTag?.id == tag.id) },
                )
            }
        }
    }
}

private fun LazyListScope.securityItems(
    securityTypes: ImmutableList<SecurityItem>,
    selectedSecurityItem: SecurityItem?,
    onClick: (SecurityItem) -> Unit
) {
    items(
        items = securityTypes,
        key = { securityType -> "SecurityType:${securityType.type.ordinal}" },
        contentType = { "SecurityType" }
    ) { securityType ->
        OptionEntry(
            iconTint = if (LocalDarkMode.current) Color(0xFF0048DE) else Color(0xFF214CE8),
            title = securityType.type.asTitle(),
            titleColor = MdtTheme.color.onSurface,
            icon = securityType.type.asIcon(),
            onClick = { onClick(securityType) },
            content = { CheckIcon(checked = securityType.type == selectedSecurityItem?.type) },
        )
    }
}

private fun LazyListScope.divider() {
    item {
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .padding(vertical = 4.dp),
            color = MdtTheme.color.outlineVariant,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            securityItems = SecurityType.entries.map { securityType ->
                SecurityItem(
                    securityType,
                    0
                )
            }.toPersistentList(),
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
            securityItems = SecurityType.entries.map { securityType ->
                SecurityItem(
                    securityType,
                    0
                )
            }.toPersistentList(),
            tags = emptyList(),
            selectedTag = Tag.Empty.copy(name = "Tag 2"),
        )
    }
}