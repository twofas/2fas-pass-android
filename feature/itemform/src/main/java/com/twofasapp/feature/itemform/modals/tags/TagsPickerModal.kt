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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.viewmodel.ProvidesViewModelStoreOwner
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.feature.tags.TagDialog
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonStyle
import com.twofasapp.core.design.foundation.checked.CheckIcon
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
    tags: List<Tag>,
    selectedTagIds: List<String>,
    forceEnableConfirmButton: Boolean,
    onDismissRequest: () -> Unit,
    onConfirmTagsSelections: (List<String>) -> Unit,
) {
    ProvidesViewModelStoreOwner {
        TagsPickerContent(
            tags = tags,
            selectedTagIds = selectedTagIds,
            forceEnableConfirmButton = forceEnableConfirmButton,
            onDismissRequest = onDismissRequest,
            onConfirmTagsSelections = onConfirmTagsSelections,
        )
    }
}

@Composable
private fun TagsPickerContent(
    viewModel: TagsPickerViewModel = koinViewModel(),
    tags: List<Tag>,
    selectedTagIds: List<String>,
    forceEnableConfirmButton: Boolean,
    onDismissRequest: () -> Unit,
    onConfirmTagsSelections: (List<String>) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.init(tags, selectedTagIds)
    }

    when (uiState.state) {
        TagsPickerUiState.State.PickerModal -> {
            Modal(
                onDismissRequest = onDismissRequest,
                headerText = MdtLocale.strings.loginTags,
                headerProperties = ModalHeaderProperties(
                    showCloseButton = true,
                ),
            ) { dismissAction ->
                ModalContent(
                    tags = uiState.tags,
                    initiallySelectedTagIds = selectedTagIds,
                    selectedTagIds = uiState.selectedTagIds,
                    forceEnableConfirmButton = forceEnableConfirmButton,
                    onAddNewTag = { viewModel.openAddTag() },
                    onToggle = { viewModel.toggleTagId(it) },
                    onConfirm = { dismissAction { onConfirmTagsSelections(uiState.selectedTagIds) } },
                )
            }
        }

        TagsPickerUiState.State.AddTagDialog -> {
            TagDialog(
                onDismissRequest = { viewModel.openPicker() },
                tag = Tag.Empty.copy(vaultId = uiState.vaultId),
                onSaveClick = { viewModel.addTag(it) },
            )
        }
    }
}

@Composable
private fun ModalContent(
    tags: List<Tag>,
    initiallySelectedTagIds: List<String>,
    selectedTagIds: List<String>,
    forceEnableConfirmButton: Boolean,
    onAddNewTag: (List<String>) -> Unit = {},
    onToggle: (String) -> Unit = {},
    onConfirm: (List<String>) -> Unit = {},
) {
    if (tags.isEmpty()) {
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
                text = "Add new Tag",
                onClick = { onAddNewTag(selectedTagIds) },
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

            tags.forEachIndexed { _, isFirst, isLast, tag ->
                item("Tag:${tag.id}", "Tag") {
                    OptionEntry(
                        title = tag.name,
                        onClick = { onToggle(tag.id) },
                        content = { CheckIcon(checked = selectedTagIds.contains(tag.id)) },
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
                        text = "Add new Tag",
                        onClick = { onAddNewTag(selectedTagIds) },
                        leadingIcon = MdtIcons.Add,
                        style = ButtonStyle.Text,
                        modifier = Modifier
                            .padding(horizontal = 4.dp),
                    )

                    Space(16.dp)

                    Button(
                        text = MdtLocale.strings.commonConfirm,
                        enabled = initiallySelectedTagIds != selectedTagIds || forceEnableConfirmButton,
                        onClick = { onConfirm(selectedTagIds) },
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
            tags = listOf(
                Tag.Empty.copy(id = "1", name = "Tag 1"),
                Tag.Empty.copy(id = "2", name = "Tag 2"),
                Tag.Empty.copy(id = "3", name = "Tag 3"),
            ),
            initiallySelectedTagIds = emptyList(),
            selectedTagIds = emptyList(),
            forceEnableConfirmButton = false,
            onAddNewTag = {},
            onToggle = {},
            onConfirm = {},
        )
    }
}

@Preview
@Composable
private fun PreviewEmpty() {
    PreviewTheme {
        ModalContent(
            tags = emptyList(),
            initiallySelectedTagIds = emptyList(),
            selectedTagIds = emptyList(),
            onAddNewTag = {},
            forceEnableConfirmButton = false,
            onToggle = {},
            onConfirm = {},
        )
    }
}