/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.copyToClipboard
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.anim.AnimatedFadeVisibility
import com.twofasapp.core.design.feature.items.LoginItemPreview
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonStyle
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.layout.ActionsRow
import com.twofasapp.core.design.foundation.lazy.isScrollingUp
import com.twofasapp.core.design.foundation.lazy.listItem
import com.twofasapp.core.design.foundation.lazy.stickyListItem
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.screen.EmptySearchResults
import com.twofasapp.core.design.foundation.screen.ScreenLoading
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.state.ScreenState
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.design.window.DeviceType
import com.twofasapp.core.design.window.currentDeviceType
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.settings.domain.SortingMethod
import com.twofasapp.feature.home.ui.home.components.HomeFab
import com.twofasapp.feature.home.ui.home.components.HomeItem
import com.twofasapp.feature.home.ui.home.components.HomeSearchBar
import com.twofasapp.feature.home.ui.home.modal.FilterModal
import com.twofasapp.feature.home.ui.home.modal.ListOptionsModal
import com.twofasapp.feature.home.ui.home.modal.SortModal
import com.twofasapp.feature.purchases.PurchasesDialog
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    openAddLogin: (String) -> Unit,
    openEditLogin: (String, String) -> Unit,
    openManageTags: () -> Unit,
    openQuickSetup: () -> Unit,
    openDeveloper: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        screenState = screenState,
        onEventConsumed = { viewModel.consumeEvent(it) },
        onAddLoginClick = openAddLogin,
        onEditLoginClick = openEditLogin,
        onCopySecretFieldToClipboard = { item, secretField ->
            viewModel.decryptSecretField(
                item = item,
                secretField = secretField,
                onDecrypted = { text -> context.copyToClipboard(text = text, isSensitive = true) },
            )
        },
        onOpenQuickSetupClick = openQuickSetup,
        onTrashConfirmed = { viewModel.trash(it) },
        onSearchQueryChange = { viewModel.search(it) },
        onSearchFocusChange = { viewModel.focusSearch(it) },
        onSortingMethodSelect = { viewModel.updateSortingMethod(it) },
        onToggleTag = { viewModel.toggleTag(it) },
        onClearTag = { viewModel.clearTag() },
        onManageTagsClick = openManageTags,
        onDeveloperClick = openDeveloper,
    )
}

@Composable
private fun Content(
    uiState: HomeUiState,
    screenState: ScreenState,
    onEventConsumed: (HomeUiEvent) -> Unit = {},
    onAddLoginClick: (String) -> Unit = {},
    onEditLoginClick: (String, String) -> Unit = { _, _ -> },
    onCopySecretFieldToClipboard: (Item, SecretField?) -> Unit = { _, _ -> },
    onOpenQuickSetupClick: () -> Unit = {},
    onTrashConfirmed: (String) -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onSearchFocusChange: (Boolean) -> Unit = {},
    onSortingMethodSelect: (SortingMethod) -> Unit = {},
    onToggleTag: (Tag) -> Unit = {},
    onClearTag: () -> Unit = {},
    onManageTagsClick: () -> Unit = {},
    onDeveloperClick: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)
    var showListOptionsModal by remember { mutableStateOf(false) }
    var showSortModal by remember { mutableStateOf(false) }
    var showFilterModal by remember { mutableStateOf(false) }
    var showPaywall by remember { mutableStateOf(false) }
    val deviceType = currentDeviceType()
    val loginsPerRow = when (deviceType) {
        DeviceType.Compact -> 1
        DeviceType.Medium -> 2
        DeviceType.Expanded -> 3
    }

    uiState.events.firstOrNull()?.let { uiEvent ->
        LaunchedEffect(Unit) {
            when (uiEvent) {
                is HomeUiEvent.OpenQuickSetup -> onOpenQuickSetupClick()
            }

            onEventConsumed(uiEvent)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                showBackButton = false,
                scrollBehavior = scrollBehavior,
                content = { Text(text = MdtLocale.strings.homeTitle, style = MdtTheme.typo.medium.xl2) },
                actions = {
                    ActionsRow {
                        if (uiState.developerModeEnabled) {
                            IconButton(
                                icon = MdtIcons.Placeholder,
                                onClick = onDeveloperClick,
                            )
                        }

                        if (screenState.content is ScreenState.Content.Success && screenState.loading.not()) {
                            Box {
                                IconButton(
                                    icon = MdtIcons.Filter,
                                    onClick = { showListOptionsModal = true },
                                )

                                if (uiState.selectedTag != null) {
                                    Icon(
                                        painter = MdtIcons.CircleFilled,
                                        contentDescription = null,
                                        tint = MdtTheme.color.notice,
                                        modifier = Modifier
                                            .size(12.dp)
                                            .align(Alignment.TopEnd)
                                            .offset(x = (-6).dp, y = 6.dp),
                                    )
                                }
                            }
                        }
                    }
                },
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(top = padding.calculateTopPadding()),
        ) {
            AnimatedFadeVisibility(visible = screenState.loading) {
                ScreenLoading(
                    modifier = Modifier.fillMaxSize(),
                )
            }

            AnimatedFadeVisibility(visible = uiState.itemsFiltered.isEmpty() && uiState.searchQuery.isNotEmpty()) {
                EmptySearchResults(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ScreenPadding),
                )
            }

            AnimatedFadeVisibility(visible = screenState.content is ScreenState.Content.Empty && screenState.loading.not() && uiState.searchQuery.isEmpty()) {
                EmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(ScreenPadding),
                    onOpenQuickSetupClick = onOpenQuickSetupClick,
                )
            }

            AnimatedFadeVisibility(visible = screenState.content is ScreenState.Content.Success && screenState.loading.not()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    stickyListItem(HomeListItem.SearchBar) {
                        HomeSearchBar(
                            modifier = Modifier
                                .background(MdtTheme.color.background)
                                .padding(horizontal = 12.dp)
                                .padding(bottom = 8.dp, top = 4.dp),
                            searchQuery = uiState.searchQuery,
                            searchFocused = uiState.searchFocused,
                            selectedTag = uiState.selectedTag,
                            onSearchQueryChange = onSearchQueryChange,
                            onSearchFocusChange = onSearchFocusChange,
                            onClearFilter = onClearTag,
                        )
                    }

                    if (uiState.itemsFiltered.isEmpty()) {
                        listItem(HomeListItem.Empty) {
                            EmptySearchResults(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = ScreenPadding, vertical = 32.dp),
                            )
                        }
                    }

                    if (loginsPerRow > 1) {
                        uiState.itemsFiltered.chunked(loginsPerRow).forEachIndexed { index, logins ->
                            listItem(HomeListItem.HomeItemsRow(index = index, ids = logins.map { it.id })) {
                                Row(
                                    modifier = Modifier.animateItem(),
                                ) {
                                    logins.forEach { item ->
                                        HomeItem(
                                            item = item,
                                            tags = uiState.tags,
                                            loginClickAction = uiState.loginClickAction,
                                            query = uiState.searchQuery,
                                            modifier = Modifier.weight(1f),
                                            onEditClick = { itemId, vaultId -> onEditLoginClick(itemId, vaultId) },
                                            onTrashConfirmed = { onTrashConfirmed(item.id) },
                                            onCopySecretFieldToClipboard = onCopySecretFieldToClipboard,
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        uiState.itemsFiltered.forEach { item ->
                            listItem(HomeListItem.HomeItem(id = item.id)) {
                                HomeItem(
                                    item = item,
                                    tags = uiState.tags,
                                    loginClickAction = uiState.loginClickAction,
                                    query = uiState.searchQuery,
                                    modifier = Modifier.animateItem(),
                                    onEditClick = { itemId, vaultId -> onEditLoginClick(itemId, vaultId) },
                                    onTrashConfirmed = { onTrashConfirmed(item.id) },
                                    onCopySecretFieldToClipboard = onCopySecretFieldToClipboard,
                                )
                            }
                        }
                    }
                }
            }

            HomeFab(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(ScreenPadding),
                visible = listState.isScrollingUp(),
                onClick = {
                    if (uiState.isItemsLimitReached) {
                        showPaywall = true
                    } else {
                        onAddLoginClick(uiState.vault.id)
                    }
                },
            )
        }
    }

    if (showListOptionsModal) {
        ListOptionsModal(
            selectedTag = uiState.selectedTag,
            onDismissRequest = { showListOptionsModal = false },
            onSortClick = { showSortModal = true },
            onFilterClick = { showFilterModal = true },
            onClearClick = { onClearTag() },
        )
    }

    if (showSortModal) {
        SortModal(
            onDismissRequest = { showSortModal = false },
            selected = uiState.sortingMethod,
            onSelect = onSortingMethodSelect,
        )
    }

    if (showFilterModal) {
        FilterModal(
            onDismissRequest = { showFilterModal = false },
            tags = uiState.tags,
            selectedTag = uiState.selectedTag,
            onToggle = { onToggleTag(it) },
            onManageTagsClick = onManageTagsClick,
        )
    }

    if (showPaywall) {
        PurchasesDialog(
            onDismissRequest = { showPaywall = false },
            title = MdtLocale.strings.paywallNoticeItemsLimitReachedTitle,
            body = MdtLocale.strings.paywallNoticeItemsLimitReachedMsg.format(uiState.maxItems),
        )
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    onOpenQuickSetupClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Space(1f)

        Icon(
            painter = MdtIcons.Key,
            contentDescription = null,
            modifier = Modifier.size(70.dp),
            tint = MdtTheme.color.secondary,
        )

        Space(24.dp)

        Text(
            text = MdtLocale.strings.homeEmptyTitle,
            style = MdtTheme.typo.titleLarge,
        )

        Space(36.dp)

        Button(
            text = MdtLocale.strings.homeEmptyImportCta,
            onClick = onOpenQuickSetupClick,
            leadingIcon = MdtIcons.RocketLaunch,
            style = ButtonStyle.Text,
            modifier = Modifier
                .clip(CircleShape)
                .background(MdtTheme.color.surfaceContainerLow),
        )

        Space(1f)
    }
}

@Preview
@Composable
private fun PreviewEmpty() {
    PreviewTheme {
        Content(
            uiState = HomeUiState(),
            screenState = ScreenState.Empty,
        )
    }
}

@Preview
@Composable
private fun PreviewSuccess() {
    PreviewTheme {
        Content(
            uiState = HomeUiState(
                items = buildList {
                    repeat(3) {
                        add(LoginItemPreview.copy(id = it.toString()))
                    }
                },
            ),
            screenState = ScreenState.Success,
        )
    }
}

@Preview
@Composable
private fun PreviewEmptySearchResults() {
    PreviewTheme {
        Content(
            uiState = HomeUiState(searchQuery = "query"),
            screenState = ScreenState.Success,
        )
    }
}