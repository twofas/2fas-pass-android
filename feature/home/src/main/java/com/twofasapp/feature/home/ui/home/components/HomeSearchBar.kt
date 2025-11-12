/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.home.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewColumn
import com.twofasapp.core.design.foundation.search.SearchBar
import com.twofasapp.core.design.foundation.text.TextIcon
import kotlinx.coroutines.android.awaitFrame

@Composable
internal fun HomeSearchBar(
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    searchFocused: Boolean = false,
    selectedTag: Tag? = null,
    onSearchQueryChange: (String) -> Unit = {},
    onSearchFocusChange: (Boolean) -> Unit = {},
    onClearFilter: () -> Unit = {},
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

    Column(
        modifier = modifier.animateContentSize(),
    ) {
        SearchBar(
            modifier = Modifier.height(50.dp),
            query = searchQuery,
            focused = searchFocused,
            onSearchQueryChange = onSearchQueryChange,
            onSearchFocusChange = onSearchFocusChange,
            focusRequester = focusRequester,
        )

        if (selectedTag != null) {
            Space(8.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(MdtTheme.color.surfaceContainer)
                    .padding(start = 18.dp, end = 8.dp, top = 2.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextIcon(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(selectedTag.name)
                        }
                        append(" (${if (selectedTag.assignedItemsCount == 1) "1 item" else "${selectedTag.assignedItemsCount} items"})")
                    },
                    leadingIcon = MdtIcons.Tag,
                    leadingIconTint = MdtTheme.color.onSurface,
                    leadingIconSize = 16.dp,
                    leadingIconSpacer = 8.dp,
                    style = MdtTheme.typo.bodyMedium,
                    modifier = Modifier.weight(1f),
                )

                IconButton(
                    icon = MdtIcons.Close,
                    iconSize = 20.dp,
                    onClick = onClearFilter,
                )
            }

            Space(8.dp)
        }
    }
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

        HomeSearchBar(
            searchFocused = false,
            selectedTag = Tag.Empty.copy(name = "Work"),
        )
    }
}