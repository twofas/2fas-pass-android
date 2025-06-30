/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.knownbrowsers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.screen.LazyContent
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.state.ScreenState
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.main.domain.ConnectedBrowser
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun KnownBrowsersScreen(
    viewModel: KnownBrowsersViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        screenState = screenState,
        onDeleteClick = viewModel::delete,
    )
}

@Composable
private fun Content(
    uiState: KnownBrowsersUiState,
    screenState: ScreenState,
    onDeleteClick: (ConnectedBrowser) -> Unit = {},
) {
    val strings = MdtLocale.strings

    Scaffold(
        topBar = { TopAppBar(title = strings.knownBrowsersTitle) },
    ) { padding ->

        LazyContent(
            screenState = screenState,
            modifier = Modifier
                .fillMaxSize()
                .background(MdtTheme.color.background),
            contentPadding = PaddingValues(top = padding.calculateTopPadding()),
            itemsAlwaysVisible = {
                item("Title", "Title") {
                    OptionEntry(
                        modifier = Modifier.animateItem(),
                        title = null,
                        subtitle = strings.knownBrowsersDescription,
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
                    )
                }
            },
            itemsWhenSuccess = {
                uiState.connectedBrowsers.forEach { browser ->
                    item("Browser:${browser.id}", "Browser") {
                        KnownBrowserItem(
                            modifier = Modifier.animateItem(),
                            browser = browser,
                            onDeleteClick = onDeleteClick,
                        )
                    }
                }
            },
            emptyIcon = MdtIcons.Desktop,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            uiState = KnownBrowsersUiState(),
            screenState = ScreenState.Success,
        )
    }
}