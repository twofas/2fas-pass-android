/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.lockoutsettings

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.data.settings.SettingsRepository
import com.twofasapp.data.settings.domain.AppLockAttempts
import com.twofasapp.data.settings.domain.AppLockTime
import com.twofasapp.data.settings.domain.AutofillLockTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class LockoutSettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val uiState = MutableStateFlow(LockoutSettingsUiState())

    init {
        launchScoped {
            settingsRepository.observeAppLockTime().collect { time ->
                uiState.update { it.copy(appLockTime = time) }
            }
        }

        launchScoped {
            settingsRepository.observeAppLockAttempts().collect { attempts ->
                uiState.update { it.copy(appLockAttempts = attempts) }
            }
        }

        launchScoped {
            settingsRepository.observeAutofillLockTime().collect { time ->
                uiState.update { it.copy(autofillLockTime = time) }
            }
        }
    }

    fun updateAppLockTime(time: AppLockTime) {
        launchScoped { settingsRepository.setAppLockTime(time) }
    }

    fun updateAppLockAttempts(attempts: AppLockAttempts) {
        launchScoped { settingsRepository.setAppLockAttempts(attempts) }
    }

    fun updateAutofillLockTime(time: AutofillLockTime) {
        launchScoped { settingsRepository.setAutofillLockTime(time) }
    }
}