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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewColumn
import com.twofasapp.core.design.foundation.search.SearchBar
import com.twofasapp.core.design.foundation.text.TextIcon
import kotlinx.coroutines.android.awaitFrame

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun HomeSearchBar(
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    searchFocused: Boolean = false,
    selectedTag: Tag? = null,
    selectedItemType: ItemContentType? = null,
    onSearchQueryChange: (String) -> Unit = {},
    onSearchFocusChange: (Boolean) -> Unit = {},
    onSelectedItemTypeChange: (ItemContentType?) -> Unit = {},
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
            modifier = Modifier
                .height(50.dp)
                .padding(horizontal = 12.dp),
            query = searchQuery,
            focused = searchFocused,
            onSearchQueryChange = onSearchQueryChange,
            onSearchFocusChange = onSearchFocusChange,
            focusRequester = focusRequester,
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
        ) {
            item {
                Tab(
                    text = "All Items",
                    icon = MdtIcons.AllItems,
                    selected = selectedItemType == null,
                    type = null,
                    onClick = {
                        focusManager.clearFocus()
                        onSelectedItemTypeChange(null)
                    },
                )
            }

            item {
                Tab(
                    text = "Logins",
                    icon = MdtIcons.Login,
                    type = ItemContentType.Login,
                    selected = selectedItemType is ItemContentType.Login,
                    onClick = {
                        focusManager.clearFocus()
                        onSelectedItemTypeChange(ItemContentType.Login)
                    },

                )
            }

            item {
                Tab(
                    text = "Secure Notes",
                    icon = MdtIcons.SecureNote,
                    type = ItemContentType.SecureNote,
                    selected = selectedItemType is ItemContentType.SecureNote,
                    onClick = {
                        focusManager.clearFocus()
                        onSelectedItemTypeChange(ItemContentType.SecureNote)
                    },
                )
            }

//            item {
//                Tab(
//                    text = "Credit Cards",
//                    icon = MdtIcons.CreditCard,
//                    type = ItemContentType.CreditCard,
//                    selected = selectedItemType is ItemContentType.CreditCard,
//                    onClick = {
//                        focusManager.clearFocus()
//                        onSelectedItemTypeChange(ItemContentType.CreditCard)
//                    },
//                )
//            }
        }

        if (selectedTag != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clip(CircleShape)
                    .background(MdtTheme.color.surfaceContainer)
                    .padding(start = 18.dp, end = 8.dp),
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

@Composable
private fun Tab(
    text: String,
    icon: Painter,
    type: ItemContentType?,
    selected: Boolean,
    onClick: () -> Unit = {},
) {
    val containerColor = if (selected) {
        type.contentColor()
    } else {
        MdtTheme.color.surfaceContainer
    }

    val contentColor = if (selected) {
        if (containerColor.luminance() > 0.5f) {
            Color.Black
        } else {
            Color.White
        }
    } else {
        MdtTheme.color.onSurfaceVariant.copy(alpha = 0.9f)
    }

    TextIcon(
        text = text,
        style = MdtTheme.typo.labelLarge,
        color = contentColor,
        leadingIcon = icon,
        leadingIconSpacer = 8.dp,
        leadingIconTint = contentColor,
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(containerColor)
            .clickable { onClick() }
            .padding(start = 16.dp, end = 20.dp, top = 6.dp, bottom = 6.dp),
    )
}

@Composable
private fun ItemContentType?.contentColor(): Color {
    return when (this) {
        ItemContentType.Login -> MdtTheme.color.itemLoginContent
        ItemContentType.SecureNote -> MdtTheme.color.itemSecureNoteContent
        ItemContentType.PaymentCard -> MdtTheme.color.itemCreditCardContent
        is ItemContentType.Unknown -> MdtTheme.color.primaryContainer
        null -> MdtTheme.color.primaryContainer
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