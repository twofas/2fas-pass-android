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
import kotlinx.coroutines.flow.MutableStateFlow

internal class StartupViewModel(
    private val purchasesRepository: PurchasesRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(StartupUiState())

    init {
        launchScoped {
            purchasesRepository.restorePurchase()
        }
    }
}