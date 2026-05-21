/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.data.purchases.PurchasesRepository
import com.twofasapp.data.settings.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

internal class StartupViewModel(
    private val startupProcessor: StartupProcessor,
    private val purchasesRepository: PurchasesRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {
    val uiState = MutableStateFlow(StartupUiState())

    init {
        launchScoped {
            purchasesRepository.restorePurchase()
        }
    }

    fun checkStartupDataValidity(onExpired: () -> Unit) {
        launchScoped {
            if (sessionRepository.observeStartupCompleted().first()) {
                return@launchScoped
            }

            val expired = startupProcessor.isStartupDataExpired()

            if (expired) {
                startupProcessor.clearStartupData()
                onExpired()
            }
        }
    }
}