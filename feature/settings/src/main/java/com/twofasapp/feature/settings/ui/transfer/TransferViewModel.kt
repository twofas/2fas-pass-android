/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.transfer

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.data.main.ItemsRepository
import com.twofasapp.data.purchases.PurchasesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class TransferViewModel(
    private val itemsRepository: ItemsRepository,
    private val purchasesRepository: PurchasesRepository,
) : ViewModel() {
    val uiState = MutableStateFlow(TransferUiState())

    init {
        launchScoped {
            purchasesRepository.observeSubscriptionPlan().collect { plan ->
                launchScoped {
                    uiState.update { state ->
                        state.copy(
                            maxItems = plan.entitlements.itemsLimit,
                            isItemsLimitReached = itemsRepository.getItemsCount() >= plan.entitlements.itemsLimit,
                        )
                    }
                }
            }
        }
    }
}