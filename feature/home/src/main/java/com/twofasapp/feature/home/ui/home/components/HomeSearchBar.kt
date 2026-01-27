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
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.domain.SecurityItem
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.tags.iconTint
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewColumn
import com.twofasapp.core.design.foundation.search.SearchBar
import com.twofasapp.core.design.foundation.text.TextIcon
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.itemform.modals.securitytype.asIcon
import com.twofasapp.feature.itemform.modals.securitytype.asTitle
import kotlinx.coroutines.android.awaitFrame

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun HomeSearchBar(
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    searchFocused: Boolean = false,
    selectedTag: Tag? = null,
    selectedSecurityItem: SecurityItem? = null,
    selectedItemType: ItemContentType? = null,
    filteredItemsCount: Int = 0,
    onSearchQueryChange: (String) -> Unit = {},
    onSearchFocusChange: (Boolean) -> Unit = {},
    onSelectedItemTypeChange: (ItemContentType?) -> Unit = {},
    onClearTagFilter: () -> Unit = {},
    onClearSecurityItemFilter: () -> Unit = {},
) {
    val strings = MdtLocale.strings
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
                    text = strings.contentTypeFilterAllName,
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
                    text = strings.contentTypeFilterLoginName,
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
                    text = strings.contentTypeFilterSecureNoteName,
                    icon = MdtIcons.SecureNote,
                    type = ItemContentType.SecureNote,
                    selected = selectedItemType is ItemContentType.SecureNote,
                    onClick = {
                        focusManager.clearFocus()
                        onSelectedItemTypeChange(ItemContentType.SecureNote)
                    },
                )
            }

            item {
                Tab(
                    text = strings.contentTypeFilterCardName,
                    icon = MdtIcons.PaymentCard,
                    type = ItemContentType.PaymentCard,
                    selected = selectedItemType is ItemContentType.PaymentCard,
                    onClick = {
                        focusManager.clearFocus()
                        onSelectedItemTypeChange(ItemContentType.PaymentCard)
                    },
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            securityItemFilter(
                securityItem = selectedSecurityItem,
                count = filteredItemsCount,
                onClearClick = onClearSecurityItemFilter
            )
            tagItemFilter(
                tag = selectedTag,
                count = filteredItemsCount,
                onClearClick = onClearTagFilter
            )
        }

        if (selectedSecurityItem != null || selectedTag != null) {
            Space(8.dp)
        }
    }
}

private fun LazyListScope.securityItemFilter(
    securityItem: SecurityItem?,
    count: Int,
    onClearClick: () -> Unit
) {
    securityItem?.let { securityItem ->
        item {
            FilterItem(
                iconTint = Color(0xFF0077FF),
                icon = securityItem.type.asIcon(),
                name = securityItem.type.asTitle(),
                count = count,
                onClearClick = onClearClick
            )
        }
    }
}

private fun LazyListScope.tagItemFilter(
    tag: Tag?,
    count: Int,
    onClearClick: () -> Unit
) {
    tag?.let { tag ->
        item {
            FilterItem(
                icon = MdtIcons.TagFilled,
                iconTint = tag.iconTint(),
                name = tag.name,
                count = count,
                onClearClick = onClearClick
            )
        }
    }
}

@Composable
private fun FilterItem(
    iconTint: Color,
    icon: Painter,
    name: String,
    count: Int,
    onClearClick: () -> Unit
) {
    val strings = MdtLocale.strings
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(MdtTheme.color.surfaceContainer)
            .padding(start = 18.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextIcon(
            text = strings.homeFilterTagWithCount.format(name, count),
            leadingIcon = icon,
            leadingIconTint = iconTint,
            leadingIconSize = 16.dp,
            leadingIconSpacer = 8.dp,
            style = MdtTheme.typo.labelLarge,
        )

        IconButton(
            icon = MdtIcons.Close,
            iconSize = 20.dp,
            onClick = onClearClick,
        )
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
        ItemContentType.PaymentCard -> MdtTheme.color.itemPaymentCardContent
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
            selectedSecurityItem = SecurityItem(SecurityType.Tier1, 1)
        )
    }
}