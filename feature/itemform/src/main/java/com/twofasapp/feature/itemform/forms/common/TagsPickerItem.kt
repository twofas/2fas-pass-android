/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform.forms.common

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.design.foundation.lazy.listItem

internal fun LazyListScope.tagsPickerItem(
    item: Item,
    tags: List<Tag>,
    onTagsChange: (List<String>) -> Unit,
) {
    listItem(FormListItem.TagsPicker) {
        val focusManager = LocalFocusManager.current

        TagsPicker(
            modifier = Modifier.animateItem(),
            tags = tags,
            item = item,
            onOpened = { focusManager.clearFocus() },
            onConfirmTagsSelections = onTagsChange,
        )
    }
}