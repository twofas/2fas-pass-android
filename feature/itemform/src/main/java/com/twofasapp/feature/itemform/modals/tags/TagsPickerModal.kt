/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform.modals.tags

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.viewmodel.ProvidesViewModelStoreOwner
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.feature.tags.TagDialog
import com.twofasapp.core.design.feature.tags.iconFilled
import com.twofasapp.core.design.feature.tags.iconTint
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonStyle
import com.twofasapp.core.design.foundation.lazy.forEachIndexed
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.modal.ModalHeaderProperties
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.theme.RoundedShapeIndexed
import com.twofasapp.core.locale.MdtLocale
import org.koin.androidx.compose.koinViewModel

@Composable
fun TagsPickerModal(
    item: Item,
    onDismissRequest: () -> Unit,
    onTagsChanged: (List<String>) -> Unit,
) {
    ProvidesViewModelStoreOwner {
        TagsPickerContent(
            items = listOf(item),
            onDismissRequest = onDismissRequest,
            onConfirm = { onTagsChanged(it.values.flatten()) },
        )
    }
}

@Composable
fun TagsPickerMultiModal(
    items: List<Item>,
    onDismissRequest: () -> Unit,
    onTagsChanged: (Map<Item, Set<String>>) -> Unit,
) {
    ProvidesViewModelStoreOwner {
        TagsPickerContent(
            items = items,
            onDismissRequest = onDismissRequest,
            onConfirm = onTagsChanged,
        )
    }
}

@Composable
private fun TagsPickerContent(
    viewModel: TagsPickerViewModel = koinViewModel(),
    items: List<Item>,
    onDismissRequest: () -> Unit,
    onConfirm: (Map<Item, Set<String>>) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddTagDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.init(items)
    }

    Modal(
        onDismissRequest = onDismissRequest,
        headerText = MdtLocale.strings.loginTags,
        headerProperties = ModalHeaderProperties(
            showCloseButton = true,
        ),
    ) { dismissAction ->
        ModalContent(
            uiState = uiState,
            onAddNewTag = { showAddTagDialog = true },
            onSelect = { viewModel.selectTag(it) },
            onDeselect = { viewModel.deselectTag(it) },
            onConfirm = { dismissAction { onConfirm(uiState.changedSelection) } },
        )
    }

    if (showAddTagDialog) {
        TagDialog(
            onDismissRequest = { showAddTagDialog = false },
            tag = Tag.Empty.copy(vaultId = uiState.vaultId),
            onSaveClick = { viewModel.addTag(it) },
        )
    }
}

@Composable
private fun ModalContent(
    uiState: TagsPickerUiState,
    onAddNewTag: () -> Unit = {},
    onSelect: (String) -> Unit = {},
    onDeselect: (String) -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    if (uiState.tags.isEmpty()) {
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
                text = MdtLocale.strings.tagsAddNewCta,
                onClick = { onAddNewTag() },
                leadingIcon = MdtIcons.Add,
                style = ButtonStyle.Text,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MdtTheme.color.surfaceContainerHigh),
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            item("Title", "Title") {
                Text(
                    text = MdtLocale.strings.loginTagsDescription,
                    style = MdtTheme.typo.bodyMedium,
                    color = MdtTheme.color.onSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                )
            }

            uiState.tags.forEachIndexed { _, isFirst, isLast, tag ->
                item {
                    val isSelected = uiState.selectedTagIds.contains(tag.id)
                    val isSelectedInAllItems = uiState.selection.values.all { it.contains(tag.id) }

                    OptionEntry(
                        icon = tag.iconFilled(),
                        iconTint = tag.iconTint(),
                        title = tag.name,
                        onClick = {
                            if (isSelected) {
                                onDeselect(tag.id)
                            } else {
                                onSelect(tag.id)
                            }
                        },
                        content = {
                            Icon(
                                painter = if (isSelected) {
                                    if (isSelectedInAllItems) {
                                        MdtIcons.CircleCheckFilled
                                    } else {
                                        MdtIcons.MinusCircle
                                    }
                                } else {
                                    MdtIcons.CircleUncheck
                                },
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (isSelected) {
                                    MdtTheme.color.primary
                                } else {
                                    MdtTheme.color.surfaceContainerHighest
                                },
                            )
                        },
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .clip(RoundedShapeIndexed(isFirst, isLast))
                            .background(MdtTheme.color.surfaceContainerHigh),
                    )
                }
            }

            item("Cta", "Cta") {
                Column {
                    Space(8.dp)

                    Button(
                        text = MdtLocale.strings.tagsAddNewCta,
                        onClick = { onAddNewTag() },
                        leadingIcon = MdtIcons.Add,
                        style = ButtonStyle.Text,
                        modifier = Modifier
                            .padding(horizontal = 4.dp),
                    )

                    Space(16.dp)

                    Button(
                        text = MdtLocale.strings.commonConfirm,
                        enabled = uiState.initialSelection != uiState.selection,
                        onClick = { onConfirm() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                    )

                    Space(16.dp)
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        ModalContent(
            uiState = TagsPickerUiState(
                tags = listOf(
                    Tag.Empty.copy(id = "1", name = "Tag 1"),
                    Tag.Empty.copy(id = "2", name = "Tag 2"),
                    Tag.Empty.copy(id = "3", name = "Tag 3"),
                ),
            ),
            onAddNewTag = {},
            onConfirm = {},
        )
    }
}