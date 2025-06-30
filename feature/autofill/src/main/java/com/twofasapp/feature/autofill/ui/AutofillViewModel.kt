/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.ui

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.auth.AuthStatusTracker
import com.twofasapp.core.common.domain.AuthStatus
import com.twofasapp.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class AutofillViewModel(
    private val settingsRepository: SettingsRepository,
    private val authStatusTracker: AuthStatusTracker,
) : ViewModel() {

    val uiState = MutableStateFlow(AutofillUiState())

    init {
        launchScoped {
            authStatusTracker.observeAuthStatus().collect { authStatus ->
                uiState.update { it.copy(authStatus = authStatus) }

                when (authStatus) {
                    is AuthStatus.Valid -> {
                        uiState.update { it.copy(showLock = false) }
                    }

                    is AuthStatus.Invalid -> {
                        val showLockImmediately = when (authStatus) {
                            AuthStatus.Invalid.AppBackgrounded -> false
                            AuthStatus.Invalid.NotAuthenticated -> true
                            AuthStatus.Invalid.SessionExpired -> true
                        }
                        uiState.update { it.copy(showLock = showLockImmediately) }
                    }
                }
            }
        }

        launchScoped {
            settingsRepository.observeSelectedTheme().collect { theme ->
                uiState.update { it.copy(selectedTheme = theme) }
            }
        }

        launchScoped {
            settingsRepository.observeDynamicColors().collect { dynamicColors ->
                uiState.update { it.copy(dynamicColors = dynamicColors) }
            }
        }
    }

    fun consumeEvent(event: AutofillUiEvent) {
        uiState.update { it.copy(events = it.events.minus(event).distinct()) }
    }

    private fun publishEvent(event: AutofillUiEvent) {
        uiState.update { it.copy(events = it.events.plus(event).distinct()) }
    }
}