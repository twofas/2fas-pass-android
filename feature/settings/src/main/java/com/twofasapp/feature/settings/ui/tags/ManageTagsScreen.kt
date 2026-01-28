package com.twofasapp.feature.settings.ui.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.feature.tags.ManageTagModal
import com.twofasapp.core.design.feature.tags.iconTint
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.dialog.ConfirmDialog
import com.twofasapp.core.design.foundation.lazy.isScrollingUp
import com.twofasapp.core.design.foundation.menu.DropdownMenu
import com.twofasapp.core.design.foundation.menu.DropdownMenuItem
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.screen.ScreenEmpty
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun ManageTagsScreen(
    viewModel: ManageTagsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        onAddTag = viewModel::addTag,
        onEditTag = viewModel::editTag,
        onDeleteTag = viewModel::deleteTag,
    )
}

@Composable
private fun Content(
    uiState: ManageTagsUiState,
    onAddTag: (Tag) -> Unit = {},
    onEditTag: (Tag) -> Unit = {},
    onDeleteTag: (Tag) -> Unit = {},
) {
    val strings = MdtLocale.strings
    val listState = rememberLazyListState()
    var clickedTag by remember { mutableStateOf(Tag.Empty) }
    var showAddTagModal by remember { mutableStateOf(false) }
    var showEditTagModal by remember { mutableStateOf(false) }
    var showDeleteTagDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = strings.settingsEntryManageTags) },
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(top = padding.calculateTopPadding()),
        ) {
            if (uiState.tags.isEmpty()) {
                ScreenEmpty(
                    modifier = Modifier.fillMaxSize(),
                    icon = MdtIcons.Tag,
                    text = strings.tagsEmptyList,
                )
            } else {
                LazyColumn(
                    state = listState,
                ) {
                    uiState.tags.forEach { tag ->
                        item(key = tag.id, contentType = "Tag") {
                            var showDropdown by remember { mutableStateOf(false) }

                            OptionEntry(
                                modifier = Modifier.animateItem(),
                                title = tag.name,
                                subtitle = strings.tagDescription.format(tag.assignedItemsCount),
                                icon = MdtIcons.TagFilled,
                                iconTint = tag.iconTint(),
                                contentPadding = PaddingValues(
                                    top = 16.dp,
                                    bottom = 16.dp,
                                    start = 16.dp,
                                    end = 8.dp,
                                ),
                                content = {
                                    DropdownMenu(
                                        visible = showDropdown,
                                        onDismissRequest = { showDropdown = false },
                                        anchor = {
                                            IconButton(
                                                icon = MdtIcons.More,
                                                iconTint = MdtTheme.color.outline,
                                                onClick = { showDropdown = true },
                                            )
                                        },
                                        content = {
                                            DropdownMenuItem(
                                                text = MdtLocale.strings.commonEdit,
                                                icon = MdtIcons.Edit,
                                                onClick = {
                                                    clickedTag = tag
                                                    showDropdown = false
                                                    showEditTagModal = true
                                                },
                                            )

                                            DropdownMenuItem(
                                                text = MdtLocale.strings.tagDeleteCta,
                                                icon = MdtIcons.Delete,
                                                contentColor = MdtTheme.color.error,
                                                onClick = {
                                                    clickedTag = tag
                                                    showDropdown = false
                                                    showDeleteTagDialog = true
                                                },
                                            )
                                        },
                                    )
                                },
                            )
                        }
                    }
                }
            }

            AddTagFab(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(ScreenPadding),
                visible = listState.isScrollingUp() || uiState.tags.isEmpty(),
                onClick = { showAddTagModal = true },
            )
        }
    }

    if (showDeleteTagDialog) {
        ConfirmDialog(
            onDismissRequest = { showDeleteTagDialog = false },
            title = strings.tagDeleteConfirmTitle,
            body = strings.tagDeleteConfirmDescription.format(clickedTag.name),
            icon = MdtIcons.Delete,
            onPositive = { onDeleteTag(clickedTag) },
        )
    }

    if (showAddTagModal) {
        ManageTagModal(
            onDismissRequest = { showAddTagModal = false },
            suggestedTagColor = uiState.suggestedTagColor,
            tag = Tag.Empty.copy(vaultId = uiState.vaultId),
            onSave = onAddTag,
        )
    }

    if (showEditTagModal) {
        ManageTagModal(
            onDismissRequest = { showEditTagModal = false },
            suggestedTagColor = uiState.suggestedTagColor,
            tag = clickedTag,
            onSave = onEditTag,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            uiState = ManageTagsUiState(
                tags = listOf(
                    Tag.Empty.copy(id = "1", name = "Tag 1"),
                    Tag.Empty.copy(id = "2", name = "Tag 2"),
                    Tag.Empty.copy(id = "3", name = "Tag 3"),
                ),
            ),
        )
    }
}

@Preview
@Composable
private fun PreviewEmpty() {
    PreviewTheme {
        Content(
            uiState = ManageTagsUiState(),
        )
    }
}