/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.subscription

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.android.ktx.onComplete
import com.twofasapp.core.android.ktx.runSafely
import com.twofasapp.core.design.state.ScreenState
import com.twofasapp.core.design.state.empty
import com.twofasapp.core.design.state.loading
import com.twofasapp.core.design.state.notLoading
import com.twofasapp.core.design.state.success
import com.twofasapp.data.main.ConnectedBrowsersRepository
import com.twofasapp.data.main.LoginsRepository
import com.twofasapp.data.purchases.PurchasesRepository
import com.twofasapp.data.purchases.domain.SubscriptionPlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

internal class ManageSubscriptionViewModel(
    private val purchasesRepository: PurchasesRepository,
    private val loginsRepository: LoginsRepository,
    private val connectedBrowsersRepository: ConnectedBrowsersRepository,
) : ViewModel() {
    val uiState = MutableStateFlow(ManageSubscriptionUiState())
    val screenState = MutableStateFlow(ScreenState.Loading)

    init {
        launchScoped {
            purchasesRepository.observeSubscriptionPlan().collect { subscriptionPlan ->
                when (subscriptionPlan) {
                    is SubscriptionPlan.Free -> {
                        uiState.update { it.copy(subscriptionPlan = null) }
                        screenState.empty("No active subscription plan found.")
                    }

                    is SubscriptionPlan.Paid -> {
                        uiState.update { it.copy(subscriptionPlan = subscriptionPlan) }
                        screenState.success()
                    }
                }
            }
        }

        launchScoped {
            loginsRepository.getLoginsCount().let { count ->
                uiState.update { it.copy(itemsCount = count) }
            }
        }

        launchScoped {
            connectedBrowsersRepository.observeBrowsers().first().let { browsers ->
                uiState.update { it.copy(browsersCount = browsers.size) }
            }
        }

        fetchSubscriptionInfo()
    }

    fun pullRefresh() {
        fetchSubscriptionInfo()
    }

    private fun fetchSubscriptionInfo() {
        screenState.loading()

        launchScoped {
            runSafely { purchasesRepository.fetchSubscriptionInfo() }
                .onComplete { screenState.notLoading() }
        }
    }
}