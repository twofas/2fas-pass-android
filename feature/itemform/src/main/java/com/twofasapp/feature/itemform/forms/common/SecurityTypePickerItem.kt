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
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.design.foundation.lazy.listItem

internal fun LazyListScope.securityTypePickerItem(
    item: Item,
    onSecurityTypeChange: (SecurityType) -> Unit,
) {
    listItem(FormListItem.SecurityTypePicker) {
        val focusManager = LocalFocusManager.current

        SecurityTypePicker(
            modifier = Modifier.animateItem(),
            securityType = item.securityType,
            onSelect = onSecurityTypeChange,
            onOpened = { focusManager.clearFocus() },
        )
    }
}