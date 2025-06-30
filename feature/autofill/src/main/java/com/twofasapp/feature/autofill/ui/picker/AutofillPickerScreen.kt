/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.ui.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.currentActivity
import com.twofasapp.core.android.ktx.getSafelyParcelable
import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.anim.AnimatedFadeVisibility
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
import com.twofasapp.feature.autofill.service.builders.IntentBuilders.EXTRA_NODE_STRUCTURE
import com.twofasapp.feature.autofill.service.builders.IntentBuilders.replyWithSuccess
import com.twofasapp.feature.autofill.service.parser.NodeStructure
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun AutofillPickerScreen(
    viewModel: AutofillPickerViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val activity = LocalContext.currentActivity

    val nodeStructure = activity.intent.extras.getSafelyParcelable<NodeStructure>(EXTRA_NODE_STRUCTURE)

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
                activity.replyWithSuccess(autofillLogin)
            }
        },
        onFillClick = {
            viewModel.fill(it) { autofillLogin ->
                activity.replyWithSuccess(autofillLogin)
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
    onFillAndRememberClick: (Login) -> Unit = {},
    onFillClick: (Login) -> Unit = {},
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)

    Scaffold(
        topBar = {
            TopAppBar(
                content = { Text(text = "Autofill", style = MdtTheme.typo.medium.xl2) },
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

            AnimatedFadeVisibility(visible = uiState.suggestedLoginsFiltered.isEmpty() && uiState.otherLoginsFiltered.isEmpty() && uiState.searchQuery.isNotEmpty()) {
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
                    text = "It looks like there are no logins yet.",
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

                    if (uiState.suggestedLoginsFiltered.isNotEmpty()) {
                        listItem(AutofillPickerListItem.Header("Suggested")) {
                            OptionHeader(
                                text = "Suggested",
                                contentPadding = OptionHeaderContentPaddingFirst,
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }

                    uiState.suggestedLoginsFiltered.forEach { login ->
                        listItem(AutofillPickerListItem.Login(id = login.id)) {
                            AutofillLoginItem(
                                login = login,
                                query = uiState.searchQuery,
                                suggested = true,
                                modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
                                onFillAndRememberClick = onFillAndRememberClick,
                                onFillClick = onFillClick,
                            )
                        }
                    }

                    if (uiState.otherLoginsFiltered.isNotEmpty() && uiState.suggestedLoginsFiltered.isNotEmpty()) {
                        listItem(AutofillPickerListItem.Header("Other")) {
                            OptionHeader(
                                text = "Other",
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }

                    uiState.otherLoginsFiltered.forEach { login ->
                        listItem(AutofillPickerListItem.Login(id = login.id)) {
                            AutofillLoginItem(
                                login = login,
                                query = uiState.searchQuery,
                                suggested = false,
                                modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
                                onFillAndRememberClick = onFillAndRememberClick,
                                onFillClick = onFillClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun Previews() {
    PreviewTheme {
        Content(
            uiState = AutofillPickerUiState(
                suggestedLogins = listOf(Login.Preview),
            ),
            screenState = ScreenState.Success,
        )
    }
}