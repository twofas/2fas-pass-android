/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.customization

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.build.Device
import com.twofasapp.core.common.domain.SelectedTheme
import com.twofasapp.data.settings.SettingsRepository
import com.twofasapp.data.settings.domain.ItemClickAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class CustomizationViewModel(
    private val settingsRepository: SettingsRepository,
    private val device: Device,
) : ViewModel() {

    val uiState = MutableStateFlow(CustomizationUiState())

    init {
        launchScoped {
            settingsRepository.observeSelectedTheme().collect { theme ->
                uiState.update { it.copy(selectedTheme = theme) }
            }
        }

        launchScoped {
            settingsRepository.observeDynamicColors().collect { dynamicColors ->
                uiState.update { it.copy(dynamicColors = dynamicColors) }
            }
        }

        launchScoped {
            settingsRepository.observeItemClickAction().collect { action ->
                uiState.update { it.copy(itemClickAction = action) }
            }
        }

        launchScoped {
            device.observeName().collect { name ->
                uiState.update { it.copy(deviceName = name) }
            }
        }
    }

    fun updateTheme(selectedTheme: SelectedTheme) {
        launchScoped { settingsRepository.setSelectedTheme(selectedTheme) }
    }

    fun updateDynamicColors(enabled: Boolean) {
        launchScoped { settingsRepository.setDynamicColors(enabled) }
    }

    fun updateItemClickAction(action: ItemClickAction) {
        launchScoped { settingsRepository.setItemClickAction(action) }
    }

    fun updateDeviceName(name: String) {
        launchScoped { device.setName(name) }
    }

    fun restoreDefaultDeviceName() {
        launchScoped { device.setName(null) }
    }
}