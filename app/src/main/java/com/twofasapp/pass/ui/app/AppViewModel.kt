/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.ui.app

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.auth.AuthStatusTracker
import com.twofasapp.core.common.domain.AuthStatus
import com.twofasapp.data.settings.SessionRepository
import com.twofasapp.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

internal class AppViewModel(
    private val sessionRepository: SessionRepository,
    private val settingsRepository: SettingsRepository,
    private val authStatusTracker: AuthStatusTracker,
) : ViewModel() {

    val uiState = MutableStateFlow(AppUiState())

    init {
        launchScoped {
            combine(
                sessionRepository.observeStartupCompleted(),
                authStatusTracker.observeAuthStatus(),
            ) { a, b -> Pair(a, b) }
                .collect { (startupCompleted, authStatus) ->
                    if (startupCompleted.not()) {
                        updateStartDestination(AppStartDestination.Startup)
                        return@collect
                    }

                    uiState.update { it.copy(authStatus = authStatus) }

                    when (authStatus) {
                        is AuthStatus.Valid -> {
                            uiState.update { it.copy(showLock = false) }
                            updateStartDestination(AppStartDestination.Main)
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

    private fun updateStartDestination(appStartDestination: AppStartDestination) {
        uiState.update { it.copy(startDestination = appStartDestination) }
    }
}