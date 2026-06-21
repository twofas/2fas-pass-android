/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.settings

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.data.cloud.domain.CloudSyncStatus
import com.twofasapp.data.main.CloudRepository
import com.twofasapp.data.purchases.PurchasesRepository
import com.twofasapp.data.settings.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class SettingsViewModel(
    private val cloudRepository: CloudRepository,
    private val purchasesRepository: PurchasesRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(SettingsUiState())

    init {
        launchScoped {
            cloudRepository.observeAggregateStatus().collect { syncStatus ->
                uiState.update { it.copy(cloudSyncError = syncStatus is CloudSyncStatus.Error) }
            }
        }

        launchScoped {
            purchasesRepository.observeSubscriptionPlan().collect { plan ->
                uiState.update { it.copy(subscriptionPlan = plan) }
            }
        }
    }
}