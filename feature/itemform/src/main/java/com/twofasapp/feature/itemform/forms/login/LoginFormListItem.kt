/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform.forms.login

import com.twofasapp.core.design.foundation.lazy.ListItem

internal sealed class LoginFormListItem(key: Any? = null, type: Any? = null) : ListItem(key, type) {
    data object AddUri : LoginFormListItem()
    data object SecurityType : LoginFormListItem()
    data object Tags : LoginFormListItem()
    data object Info : LoginFormListItem()
    data class Field(val name: String) : LoginFormListItem("Field:$$name", "Field")
}