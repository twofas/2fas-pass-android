package com.twofasapp.feature.home.ui.home.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.ktx.uniform
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.dialog.ConfirmDialog
import com.twofasapp.core.design.foundation.layout.ActionsRow
import com.twofasapp.core.design.foundation.preview.PreviewAllThemesInColumn
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.state.ScreenState
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.home.ui.home.HomeUiState
import com.twofasapp.feature.itemform.modals.securitytype.SecurityTypeModal
import com.twofasapp.feature.itemform.modals.tags.TagsPickerModal

@Composable
internal fun HomeAppBar(
    uiState: HomeUiState,
    screenState: ScreenState,
    scrollBehavior: TopAppBarScrollBehavior,
    onDeveloperClick: () -> Unit = {},
    onChangeEditMode: (Boolean) -> Unit = {},
    onSortClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onClearFiltersClick: () -> Unit = {},
    onSelectAllClick: () -> Unit = {},
    onDeselectClick: () -> Unit = {},
    onDeleteItemsConfirmed: () -> Unit = {},
    onChangeSecurityType: (SecurityType) -> Unit = {},
    onChangeTags: (List<String>) -> Unit = {},
) {
    var showDeleteConfirmationPrompt by remember { mutableStateOf(false) }
    var showSecurityTypePicker by remember { mutableStateOf(false) }
    var showTagsPicker by remember { mutableStateOf(false) }

    BackHandler(uiState.editMode) {
        onChangeEditMode(false)
    }

    AnimatedContent(
        targetState = uiState.editMode,
        transitionSpec = {
            (
                slideInVertically(
                    animationSpec = tween(300),
                    initialOffsetY = { _ -> -50 },
                ) + fadeIn(
                    animationSpec = tween(100),
                )
                )
                .togetherWith(
                    slideOutVertically(
                        animationSpec = tween(300),
                        targetOffsetY = { _ -> 50 },
                    ) + fadeOut(
                        animationSpec = tween(100),
                    ),
                )
                .using(
                    SizeTransform(clip = false),
                )
        },
        label = "topBarAnimation",
    ) { editMode ->
        if (editMode) {
            TopAppBar(
                showBackButton = false,
                scrollBehavior = scrollBehavior,
                content = {
                    Row(
                        modifier = Modifier.fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            icon = MdtIcons.Close,
                            onClick = { onChangeEditMode(false) },
                            modifier = Modifier.offset(x = (-10).dp),
                        )

                        Text(
                            text = when (val count = uiState.selectedItemIds.size) {
                                0 -> ""
                                1 -> "1 item"
                                else -> "$count items"
                            },
                            style = MdtTheme.typo.medium.lg,
                        )
                    }
                },
                actions = {
                    ActionsRow(
                        spacing = 8.dp,
                    ) {
                        IconButton(
                            icon = if (uiState.items.size == uiState.selectedItemIds.size) MdtIcons.Deselect else MdtIcons.SelectAll,
                            onClick = if (uiState.items.size == uiState.selectedItemIds.size) onDeselectClick else onSelectAllClick,
                        )

                        IconButton(
                            icon = MdtIcons.Delete,
                            enabled = uiState.selectedItemIds.isNotEmpty(),
                            onClick = { showDeleteConfirmationPrompt = true },
                        )

                        IconButton(
                            icon = MdtIcons.Tier2,
                            enabled = uiState.selectedItemIds.isNotEmpty(),
                            onClick = { showSecurityTypePicker = true },
                        )

                        IconButton(
                            icon = MdtIcons.Tag,
                            enabled = uiState.selectedItemIds.isNotEmpty(),
                            onClick = { showTagsPicker = true },
                        )
                    }
                },
            )
        } else {
            TopAppBar(
                showBackButton = false,
                scrollBehavior = scrollBehavior,
                content = {
                    Row(
                        modifier = Modifier.fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = MdtLocale.strings.homeTitle,
                            style = MdtTheme.typo.medium.xl2,
                        )
                    }
                },
                actions = {
                    ActionsRow(
                        spacing = 8.dp,
                    ) {
                        if (uiState.developerModeEnabled) {
                            IconButton(
                                icon = MdtIcons.Placeholder,
                                onClick = onDeveloperClick,
                                modifier = Modifier.alpha(0.1f),
                            )
                        }

                        if (screenState.content is ScreenState.Content.Success && screenState.loading.not()) {
                            HomeListDropdownMenu(
                                selectedTag = uiState.selectedTag,
                                onEditListClick = { onChangeEditMode(true) },
                                onSortClick = { onSortClick() },
                                onFilterClick = { onFilterClick() },
                                onClearFiltersClick = { onClearFiltersClick() },
                            )
                        }
                    }
                },
            )
        }
    }

    if (showDeleteConfirmationPrompt) {
        ConfirmDialog(
            onDismissRequest = { showDeleteConfirmationPrompt = false },
            title = if (uiState.selectedItemIds.size == 1) {
                MdtLocale.strings.loginDeleteConfirmTitle
            } else {
                MdtLocale.strings.homeDeleteConfirmTitle
            },
            body = if (uiState.selectedItemIds.size == 1) {
                MdtLocale.strings.loginDeleteConfirmBody
            } else {
                MdtLocale.strings.homeDeleteConfirmBody.format(uiState.selectedItemIds.size)
            },
            icon = MdtIcons.Delete,
            onPositive = { onDeleteItemsConfirmed() },
        )
    }

    if (showSecurityTypePicker) {
        val selectedItems = uiState.selectedItems
        val selectedSecurityType = selectedItems.map { it.securityType }.uniform()

        SecurityTypeModal(
            onDismissRequest = { showSecurityTypePicker = false },
            onSelect = { onChangeSecurityType(it) },
            selected = selectedSecurityType,
        )
    }

    if (showTagsPicker) {
        val selectedItems = uiState.selectedItems
        val selectedTagIds = selectedItems.map { it.tagIds }.flatten().distinct()

        TagsPickerModal(
            onDismissRequest = { showTagsPicker = false },
            tags = uiState.tags,
            selectedTagIds = selectedTagIds,
            forceEnableConfirmButton = true,
            onConfirmTagsSelections = onChangeTags,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewAllThemesInColumn {
        HomeAppBar(
            uiState = HomeUiState(),
            screenState = ScreenState.Success,
            scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
        )
    }
}

@Preview
@Composable
private fun PreviewEditMode() {
    PreviewAllThemesInColumn {
        HomeAppBar(
            uiState = HomeUiState(editMode = true, selectedItemIds = listOf("", "", "")),
            screenState = ScreenState.Success,
            scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
        )
    }
}