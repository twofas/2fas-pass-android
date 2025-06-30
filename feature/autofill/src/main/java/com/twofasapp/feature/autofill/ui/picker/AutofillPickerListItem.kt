/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.ui.picker

import com.twofasapp.core.design.foundation.lazy.ListItem

internal sealed class AutofillPickerListItem(key: Any? = null, type: Any? = null) : ListItem(key, type) {
    data object SearchBar : AutofillPickerListItem()
    data class Header(private val text: String) : AutofillPickerListItem("Header:$text", "Header")
    data class Login(private val id: String) : AutofillPickerListItem("Login:$id", "Login")
}