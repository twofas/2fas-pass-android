/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.settings

import com.twofasapp.core.design.foundation.lazy.ListItem

internal sealed class SettingsListItem(key: Any? = null, type: Any? = null) : ListItem(key, type) {
    data class Header(private val name: String) : SettingsListItem("Header:$name", "Header")
    data class Entry(private val name: String) : SettingsListItem("Entry:$name", "Entry")
    data class Switch(private val name: String) : SettingsListItem("Switch:$name", "Switch")
}