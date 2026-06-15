/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.createsecretkey.create

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.data.main.SecurityRepository
import com.twofasapp.feature.startup.ui.StartupProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class CreateSecretKeyViewModel(
    private val securityRepository: SecurityRepository,
    private val startupProcessor: StartupProcessor,
) : ViewModel() {

    val uiState = MutableStateFlow(CreateSecretKeyUiState())

    fun generateSeed(onComplete: () -> Unit) {
        uiState.update { it.copy(loading = true) }

        launchScoped {
            startupProcessor.setSeed(securityRepository.generateSeed())
        }.invokeOnCompletion {
            uiState.update { it.copy(loading = false) }
            onComplete()
        }
    }
}