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
import com.twofasapp.data.cloud.domain.CloudConnection
import com.twofasapp.data.main.CloudRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class CloudSyncViewModel(
    private val cloudRepository: CloudRepository,
) : ViewModel() {
    val uiState = MutableStateFlow(CloudSyncUiState())

    init {
        launchScoped {
            cloudRepository.observeConfigs().collect { configs ->
                uiState.update { it.copy(configs = configs) }
            }
        }
    }

    fun removeConfig(id: String) {
        launchScoped {
            cloudRepository.removeConfig(id)
        }
    }

    fun sync(forceReplace: Boolean = false) {
        launchScoped {
            cloudRepository.sync(forceReplace = forceReplace)
        }
    }

    fun startGoogleAuth() {
        uiState.update { it.copy(startGoogleAuth = true) }
    }

    fun cancelGoogleAuth() {
        uiState.update { it.copy(startGoogleAuth = false) }
    }

    fun onGoogleAuthenticated(spec: CloudConnection.GoogleDrive) {
        uiState.update { it.copy(startGoogleAuth = false) }
        launchScoped {
            cloudRepository.addConfig(spec)
        }
    }
}