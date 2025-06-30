/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.foundation.lazy

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

fun LazyListScope.listItem(
    type: ListItem,
    content: @Composable LazyItemScope.() -> Unit,
) {
    item(key = type.key, contentType = type.type, content = content)
}

fun <T> LazyListScope.listItems(
    items: List<T>,
    type: ((item: T) -> ListItem),
    itemContent: @Composable LazyItemScope.(item: T) -> Unit,
) {
    items(
        count = items.size,
        key = { index: Int -> type(items[index]).key },
        contentType = { index: Int -> type(items[index]).type },
    ) {
        itemContent(items[it])
    }
}

fun LazyListScope.stickyListItem(
    type: ListItem,
    content: @Composable LazyItemScope.(Int) -> Unit,
) {
    stickyHeader(key = type.key, contentType = type.type, content = content)
}

inline fun <T> Iterable<T>.forEachIndexed(action: (index: Int, isFirst: Boolean, isLast: Boolean, item: T) -> Unit) {
    forEachIndexed { index, item ->
        action(index, index == 0, index == (this as Collection).size - 1, item)
    }
}

@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}