/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform.forms.login.composables

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.twofasapp.core.android.ktx.screenWidth
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.lazy.forEachIndexed
import com.twofasapp.core.design.foundation.preview.PreviewAllThemesInRow

@Composable
internal fun UsernameSuggestionsBar(
    modifier: Modifier = Modifier,
    usernameSuggestions: List<String>,
    onUsernameClick: (String) -> Unit = {},
) {
    val lazyListState = rememberLazyListState()
    val maxItemWidth: Dp = when (usernameSuggestions.size) {
        0 -> screenWidth
        1 -> screenWidth
        else -> screenWidth / 2
    }

    LaunchedEffect(usernameSuggestions) {
        lazyListState.scrollToItem(0)
    }

    LazyRow(
        modifier = modifier
            .height(48.dp)
            .background(MdtTheme.color.surfaceContainerHigh),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = 8.dp),
        state = lazyListState,
    ) {
        if (usernameSuggestions.isEmpty()) {
            item(key = "Empty", contentType = "Empty") {
                Text(
                    text = "No username suggestions available",
                    style = MdtTheme.typo.regular.sm,
                    color = MdtTheme.color.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .animateItem(
                            fadeInSpec = spring(stiffness = Spring.StiffnessMedium),
                            fadeOutSpec = spring(stiffness = Spring.StiffnessMedium),
                        ),
                )
            }
        }

        usernameSuggestions.distinct().forEachIndexed { _, _, isLast, username ->
            item(key = username, contentType = "Username") {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .animateItem(
                            fadeInSpec = spring(stiffness = Spring.StiffnessMedium),
                            fadeOutSpec = spring(stiffness = Spring.StiffnessMedium),
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = username,
                        style = MdtTheme.typo.medium.base,
                        color = MdtTheme.color.primary,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .widthIn(max = maxItemWidth)
                            .clip(CircleShape)
                            .clickable { onUsernameClick(username) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    )

                    if (isLast.not()) {
                        VerticalDivider(
                            modifier = Modifier.padding(vertical = 14.dp, horizontal = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewAllThemesInRow {
        UsernameSuggestionsBar(
            modifier = Modifier.fillMaxWidth(),
            usernameSuggestions = buildList {
                repeat(5) {
                    add("user$it@mail.com")
                }
            },
        )

        UsernameSuggestionsBar(
            modifier = Modifier.fillMaxWidth(),
            usernameSuggestions = emptyList(),
        )
    }
}