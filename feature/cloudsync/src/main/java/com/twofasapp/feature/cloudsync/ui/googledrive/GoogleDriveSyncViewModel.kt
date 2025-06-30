/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.cloudsync.ui.googledrive

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.locale.Strings
import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.main.CloudRepository
import com.twofasapp.data.main.domain.CloudSyncStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

internal class GoogleDriveSyncViewModel(
    private val cloudRepository: CloudRepository,
    private val strings: Strings,
) : ViewModel() {
    val uiState = MutableStateFlow(
        GoogleDriveSyncUiState(
            startAuth = false,
        ),
    )

    init {
        launchScoped {
            combine(
                cloudRepository.observeSyncInfo(),
                cloudRepository.observeSyncStatus(),
            ) { a, b -> Pair(a, b) }.collect { (syncInfo, syncStatus) ->
                uiState.update { state ->
                    state.copy(
                        enabled = syncInfo.enabled,
                        syncing = syncStatus == CloudSyncStatus.Syncing,
                    )
                }
            }
        }
    }

    fun enableSync(cloudConfig: CloudConfig) {
        uiState.update { it.copy(startAuth = false) }
        launchScoped { cloudRepository.enableSync(cloudConfig) }
    }

    fun disableSync() {
        launchScoped { cloudRepository.disableSync() }
    }

    fun sync() {
        launchScoped { cloudRepository.sync(forceReplace = false) }
    }
}