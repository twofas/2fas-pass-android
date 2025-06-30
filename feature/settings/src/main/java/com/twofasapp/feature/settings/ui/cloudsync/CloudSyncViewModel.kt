/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.cloudsync

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.data.main.CloudRepository
import kotlinx.coroutines.flow.MutableStateFlow

internal class CloudSyncViewModel(
    private val cloudRepository: CloudRepository,
) : ViewModel() {
    val uiState = MutableStateFlow(
        CloudSyncUiState(),
    )

    init {
        launchScoped {
            cloudRepository.observeSyncInfo().collect { syncInfo ->
                uiState.value = uiState.value.copy(
                    config = syncInfo.config,
                )
            }
        }
    }
}