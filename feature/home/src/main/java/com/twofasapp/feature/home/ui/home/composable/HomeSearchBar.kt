/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.home.composable

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.R
import com.twofasapp.core.design.foundation.preview.PreviewColumn
import com.twofasapp.core.design.foundation.search.SearchBar
import kotlinx.coroutines.android.awaitFrame

@Composable
internal fun HomeSearchBar(
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    searchFocused: Boolean = false,
    onSearchQueryChange: (String) -> Unit = {},
    onSearchFocusChange: (Boolean) -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        if (searchFocused) {
            awaitFrame()
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(searchFocused) {
        if (searchFocused.not()) {
            focusManager.clearFocus()
        }
    }

    BackHandler(enabled = searchFocused) {
        when {
            searchQuery.isNotEmpty() -> onSearchQueryChange("")
            searchFocused -> onSearchFocusChange(false)
        }
    }

    SearchBar(
        modifier = modifier.height(50.dp),
        query = searchQuery,
        focused = searchFocused,
        onSearchQueryChange = onSearchQueryChange,
        onSearchFocusChange = onSearchFocusChange,
        focusRequester = focusRequester,
        startContent = {
            Image(
                painter = painterResource(id = R.drawable.brand_logo),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        },
    )
}

@Preview
@Composable
private fun Previews() {
    PreviewColumn {
        HomeSearchBar(
            searchFocused = true,
        )

        HomeSearchBar(
            searchQuery = "query",
            searchFocused = true,
        )

        HomeSearchBar(
            searchFocused = false,
        )

        HomeSearchBar(
            searchQuery = "query",
            searchFocused = false,
        )
    }
}