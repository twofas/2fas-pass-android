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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.copyToClipboard
import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.anim.AnimatedFadeVisibility
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.button.TextButton
import com.twofasapp.core.design.foundation.layout.ActionsRow
import com.twofasapp.core.design.foundation.lazy.isScrollingUp
import com.twofasapp.core.design.foundation.lazy.listItem
import com.twofasapp.core.design.foundation.lazy.stickyListItem
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.screen.EmptySearchResults
import com.twofasapp.core.design.foundation.screen.ScreenLoading
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.state.ScreenState
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.settings.domain.SortingMethod
import com.twofasapp.feature.home.ui.home.composable.HomeFab
import com.twofasapp.feature.home.ui.home.composable.HomeSearchBar
import com.twofasapp.feature.home.ui.home.composable.LoginItem
import com.twofasapp.feature.home.ui.home.modal.FilterModal
import com.twofasapp.feature.purchases.PurchasesDialog
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    openAddLogin: (String) -> Unit,
    openEditLogin: (String, String) -> Unit,
    openSettings: () -> Unit,
    openDeveloper: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        screenState = screenState,
        onEventConsumed = { viewModel.consumeEvent(it) },
        onAddLoginClick = openAddLogin,
        onEditLoginClick = openEditLogin,
        onCopyPasswordToClipboard = { viewModel.copyPasswordToClipboard(it) },
        onOpenSettingsClick = { viewModel.openSettingsAndScrollToTransferSection { openSettings() } },
        onTrashConfirmed = { viewModel.trash(it) },
        onSearchQueryChange = { viewModel.search(it) },
        onSearchFocusChange = { viewModel.focusSearch(it) },
        onSortingMethodSelect = { viewModel.updateSortingMethod(it) },
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
    onCopyPasswordToClipboard: (Login) -> Unit = {},
    onOpenSettingsClick: () -> Unit = {},
    onTrashConfirmed: (String) -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onSearchFocusChange: (Boolean) -> Unit = {},
    onSortingMethodSelect: (SortingMethod) -> Unit = {},
    onDeveloperClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)
    var showFilterModal by remember { mutableStateOf(false) }
    var showPaywall by remember { mutableStateOf(false) }

    uiState.events.firstOrNull()?.let { uiEvent ->
        LaunchedEffect(Unit) {
            when (uiEvent) {
                is HomeUiEvent.CopyPasswordToClipboard -> {
                    context.copyToClipboard(text = uiEvent.text, isSensitive = true)
                }
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
                            IconButton(
                                icon = MdtIcons.Filter,
                                onClick = { showFilterModal = true },
                            )
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

            AnimatedFadeVisibility(visible = uiState.loginsFiltered.isEmpty() && uiState.searchQuery.isNotEmpty()) {
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
                    onOpenSettingsClick = onOpenSettingsClick,
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
                            onSearchQueryChange = onSearchQueryChange,
                            onSearchFocusChange = onSearchFocusChange,
                        )
                    }

                    uiState.loginsFiltered.forEach { login ->
                        listItem(HomeListItem.Login(id = login.id)) {
                            LoginItem(
                                login = login,
                                loginClickAction = uiState.loginClickAction,
                                query = uiState.searchQuery,
                                modifier = Modifier.animateItem(),
                                onEditClick = { onEditLoginClick(login.id, login.vaultId) },
                                onTrashConfirmed = { onTrashConfirmed(login.id) },
                                onCopyPasswordToClipboard = onCopyPasswordToClipboard,
                            )
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

    if (showFilterModal) {
        FilterModal(
            onDismissRequest = { showFilterModal = false },
            selected = uiState.sortingMethod,
            onSelect = onSortingMethodSelect,
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
    onOpenSettingsClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = MdtIcons.Key,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MdtTheme.color.secondary,
        )

        Text(
            text = MdtLocale.strings.homeEmptyTitle,
            style = MdtTheme.typo.bold.lg,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = MdtLocale.strings.homeEmptyMsg,
            style = MdtTheme.typo.regular.sm,
            color = MdtTheme.color.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = MdtLocale.strings.homeEmptyMsgImport,
            style = MdtTheme.typo.regular.sm,
            color = MdtTheme.color.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        TextButton(
            text = MdtLocale.strings.homeEmptyImportCta,
            onClick = onOpenSettingsClick,
        )
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
                logins = buildList {
                    repeat(3) {
                        add(Login.Preview.copy(id = "$it"))
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