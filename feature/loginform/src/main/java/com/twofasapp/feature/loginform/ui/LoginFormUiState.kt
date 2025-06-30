/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.loginform.ui

import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.common.domain.PasswordGeneratorSettings

internal data class LoginFormUiState(
    val initialised: Boolean = false,
    val initialLogin: Login = Login.Empty,
    val login: Login = Login.Empty,
    val usernameSuggestions: List<String> = emptyList(),
    val passwordGeneratorSettings: PasswordGeneratorSettings = PasswordGeneratorSettings(),
) {
    val valid: Boolean
        get() = login.name.isNotEmpty() && login.notes.orEmpty().length <= 2048

    val hasUnsavedChanges: Boolean
        get() = initialLogin != login

    val usernameSuggestionsFiltered: List<String>
        get() = usernameSuggestions
            .filter { it.contains(login.username.orEmpty().trim(), false) }
            .distinctBy { it.trim().lowercase() }
            .take(8)
}