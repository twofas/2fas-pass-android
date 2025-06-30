/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.securitytier

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.domain.LoginSecurityType
import com.twofasapp.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class SecurityTierViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val uiState = MutableStateFlow(SecurityTierUiState())

    init {
        launchScoped {
            settingsRepository.observeDefaultSecurityType().collect { defaultSecurityLevel ->
                uiState.update { it.copy(defaultSecurityLevel = defaultSecurityLevel) }
            }
        }
    }

    fun onChange(loginSecurityType: LoginSecurityType) {
        launchScoped {
            settingsRepository.setDefaultSecurityType(loginSecurityType)
        }
    }
}