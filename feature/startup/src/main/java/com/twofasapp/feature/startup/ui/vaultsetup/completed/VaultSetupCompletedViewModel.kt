/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.vaultsetup.completed

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.auth.AuthStatusTracker
import com.twofasapp.data.settings.SessionRepository
import com.twofasapp.feature.startup.ui.StartupConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class VaultSetupCompletedViewModel(
    private val sessionRepository: SessionRepository,
    private val startupConfig: StartupConfig,
    private val authStatusTracker: AuthStatusTracker,
) : ViewModel() {

    val uiState = MutableStateFlow(VaultSetupCompletedUiState())

    fun completeStartup() {
        uiState.update { it.copy(loading = true) }

        launchScoped {
            uiState.update { it.copy(loading = false) }

            startupConfig.finishStartup()
            authStatusTracker.authenticate()
            sessionRepository.setStartupCompleted(true)
        }
    }
}