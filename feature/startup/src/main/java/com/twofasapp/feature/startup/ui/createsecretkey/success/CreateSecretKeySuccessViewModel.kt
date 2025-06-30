/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.createsecretkey.success

import androidx.lifecycle.ViewModel
import com.twofasapp.feature.startup.ui.StartupConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class CreateSecretKeySuccessViewModel(
    private val startupConfig: StartupConfig,
) : ViewModel() {

    val uiState = MutableStateFlow(CreateSecretKeySuccessUiState())

    init {

        uiState.update {
            it.copy(words = startupConfig.seed?.words ?: emptyList())
        }
    }
}