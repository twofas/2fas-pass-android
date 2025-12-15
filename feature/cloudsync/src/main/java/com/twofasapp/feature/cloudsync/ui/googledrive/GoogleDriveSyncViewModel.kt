/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.cloudsync.ui.googledrive

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.android.navigation.Screen
import com.twofasapp.core.common.time.TimeProvider
import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.main.CloudRepository
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.main.domain.CloudSyncStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

internal class GoogleDriveSyncViewModel(
    savedStateHandle: SavedStateHandle,
    private val cloudRepository: CloudRepository,
    private val vaultsRepository: VaultsRepository,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val openedFromQuickSetup = savedStateHandle.toRoute<Screen.GoogleDriveSync>().openedFromQuickSetup
    private val startAuth = savedStateHandle.toRoute<Screen.GoogleDriveSync>().startAuth

    val uiState = MutableStateFlow(
        GoogleDriveSyncUiState(
            openedFromQuickSetup = openedFromQuickSetup,
            startAuth = startAuth,
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
        launchScoped {
            cloudRepository.sync(forceReplace = false)

            vaultsRepository.setUpdatedTimestamp(
                id = vaultsRepository.getVault().id,
                timestamp = timeProvider.currentTimeUtc(),
            )
        }
    }
}