/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform.forms.login

import com.twofasapp.core.common.domain.PasswordGeneratorSettings

internal data class LoginFormUiState(
    val usernameSuggestions: List<String> = emptyList(),
    val usernameSuggestionsFiltered: List<String> = emptyList(),
    val passwordGeneratorSettings: PasswordGeneratorSettings = PasswordGeneratorSettings(),
)