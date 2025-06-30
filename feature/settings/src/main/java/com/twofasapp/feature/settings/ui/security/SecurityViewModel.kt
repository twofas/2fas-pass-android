/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.security

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.data.main.SecurityRepository
import com.twofasapp.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class SecurityViewModel(
    private val securityRepository: SecurityRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val uiState = MutableStateFlow(SecurityUiState())

    init {
        launchScoped {
            securityRepository.observeBiometricsEnabled().collect { biometricsEnabled ->
                uiState.update { it.copy(biometricsEnabled = biometricsEnabled) }
            }
        }

        launchScoped {
            settingsRepository.observeScreenCaptureEnabled().collect { screenCaptureEnabled ->
                uiState.update { it.copy(screenCaptureEnabled = screenCaptureEnabled) }
            }
        }

        launchScoped {
            settingsRepository.observeDefaultSecurityType().collect { securityType ->
                uiState.update { it.copy(defaultSecurityType = securityType) }
            }
        }
    }

    fun updateBiometrics(enabled: Boolean) {
        launchScoped { securityRepository.saveBiometricsEnabled(enabled) }
    }

    fun saveMasterKeyEncryptedWithBiometrics(masterKey: EncryptedBytes?) {
        launchScoped { securityRepository.saveMasterKeyEncryptedWithBiometrics(masterKey) }
    }

    fun toggleScreenCapture() {
        launchScoped {
            settingsRepository.setScreenCaptureEnabled(uiState.value.screenCaptureEnabled.not())
        }
    }
}