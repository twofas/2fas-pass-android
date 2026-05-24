/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.autofill.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.copyToClipboard
import com.twofasapp.core.android.ktx.currentActivity
import com.twofasapp.core.android.ktx.getSafelyParcelable
import com.twofasapp.core.common.domain.clearTextOrNull
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.anim.AnimatedFadeVisibility
import com.twofasapp.core.design.feature.items.LoginItemPreview
import com.twofasapp.core.design.feature.settings.OptionHeader
import com.twofasapp.core.design.feature.settings.OptionHeaderContentPaddingFirst
import com.twofasapp.core.design.foundation.lazy.listItem
import com.twofasapp.core.design.foundation.lazy.stickyListItem
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.screen.EmptySearchResults
import com.twofasapp.core.design.foundation.screen.ScreenEmpty
import com.twofasapp.core.design.foundation.screen.ScreenLoading
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.state.ScreenState
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.autofill.service.parser.NodeStructure
import com.twofasapp.feature.home.ui.autofill.AutofillActivity.Companion.EXTRA_NODE_STRUCTURE
import com.twofasapp.feature.home.ui.autofill.replyWithAutofillSuccess
import com.twofasapp.feature.home.ui.home.components.HomeFab
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun AutofillPickerScreen(
    viewModel: AutofillPickerViewModel = koinViewModel(),
    openAddLogin: () -> Unit = {},
    openEditLogin: (Item) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val activity = LocalContext.currentActivity
    val context = LocalContext.current

    val nodeStructure = activity.intent.extras.getSafelyParcelable<NodeStructure>(EXTRA_NODE_STRUCTURE)

    if (nodeStructure == null) {
        activity.finishAffinity()
        return
    }

    LaunchedEffect(Unit) {
        viewModel.init(nodeStructure)
    }

    Content(
        uiState = uiState,
        screenState = screenState,
        onSearchQueryChange = { viewModel.search(it) },
        onSearchFocusChange = { viewModel.focusSearch(it) },
        onFillAndRememberClick = {
            viewModel.fillAndRemember(it) { autofillLogin ->
                activity.replyWithAutofillSuccess(autofillLogin)
            }
        },
        onFillClick = {
            viewModel.fill(it) { autofillLogin ->
                activity.replyWithAutofillSuccess(autofillLogin)
            }
        },
        onAddClick = openAddLogin,
        onEditClick = openEditLogin,
        onCopyUsernameClick = { item ->
            (item.content as? ItemContent.Login)?.username?.let { username ->
                context.copyToClipboard(text = username)
            }
        },
        onCopyPasswordClick = { item ->
            (item.content as? ItemContent.Login)?.password?.clearTextOrNull?.let { password ->
                context.copyToClipboard(text = password, isSensitive = true)
            }
        },
    )
}

@Composable
private fun Content(
    uiState: AutofillPickerUiState,
    screenState: ScreenState,
    onSearchQueryChange: (String) -> Unit = {},
    onSearchFocusChange: (Boolean) -> Unit = {},
    onFillAndRememberClick: (Item) -> Unit = {},
    onFillClick: (Item) -> Unit = {},
    onAddClick: () -> Unit = {},
    onEditClick: (Item) -> Unit = {},
    onCopyUsernameClick: (Item) -> Unit = {},
    onCopyPasswordClick: (Item) -> Unit = {},
) {
    val strings = MdtLocale.strings
    val listState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)

    Scaffold(
        topBar = {
            TopAppBar(
                content = { Text(text = strings.autofillLoginDialogTitle, style = MdtTheme.typo.medium.xl2) },
                scrollBehavior = scrollBehavior,
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

            AnimatedFadeVisibility(visible = uiState.suggestedItemsFiltered.isEmpty() && uiState.otherItemsFiltered.isEmpty() && uiState.searchQuery.isNotEmpty()) {
                EmptySearchResults(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ScreenPadding),
                )
            }

            AnimatedFadeVisibility(visible = screenState.content is ScreenState.Content.Empty && screenState.loading.not() && uiState.searchQuery.isEmpty()) {
                ScreenEmpty(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(ScreenPadding),
                    text = strings.autofillPickerEmptyState,
                    icon = MdtIcons.Info,
                )
            }

            AnimatedFadeVisibility(visible = screenState.content is ScreenState.Content.Success && screenState.loading.not()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                ) {
                    stickyListItem(AutofillPickerListItem.SearchBar) {
                        AutofillSearchbar(
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

                    if (uiState.suggestedItemsFiltered.isNotEmpty()) {
                        listItem(AutofillPickerListItem.Header(strings.commonSuggested)) {
                            OptionHeader(
                                text = strings.commonSuggested,
                                contentPadding = OptionHeaderContentPaddingFirst,
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }

                    uiState.suggestedItemsFiltered.forEach { item ->
                        listItem(AutofillPickerListItem.AutofillItem(id = item.id)) {
                            AutofillLoginItem(
                                item = item,
                                query = uiState.searchQuery,
                                suggested = true,
                                modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
                                onFillAndRememberClick = onFillAndRememberClick,
                                onFillClick = onFillClick,
                                onEditClick = onEditClick,
                                onCopyUsernameClick = onCopyUsernameClick,
                                onCopyPasswordClick = onCopyPasswordClick,
                            )
                        }
                    }

                    if (uiState.otherItemsFiltered.isNotEmpty() && uiState.suggestedItemsFiltered.isNotEmpty()) {
                        listItem(AutofillPickerListItem.Header(strings.commonOther)) {
                            OptionHeader(
                                text = strings.commonOther,
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }

                    uiState.otherItemsFiltered.forEach { item ->
                        listItem(AutofillPickerListItem.AutofillItem(id = item.id)) {
                            AutofillLoginItem(
                                item = item,
                                query = uiState.searchQuery,
                                suggested = false,
                                modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
                                onFillAndRememberClick = onFillAndRememberClick,
                                onFillClick = onFillClick,
                                onEditClick = onEditClick,
                                onCopyUsernameClick = onCopyUsernameClick,
                                onCopyPasswordClick = onCopyPasswordClick,
                            )
                        }
                    }
                }
            }

            HomeFab(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(ScreenPadding),
                visible = screenState.loading.not(),
                onClick = onAddClick,
            )
        }
    }
}

@Preview
@Composable
private fun Previews() {
    PreviewTheme {
        Content(
            uiState = AutofillPickerUiState(
                suggestedItems = listOf(LoginItemPreview),
            ),
            screenState = ScreenState.Success,
        )
    }
}