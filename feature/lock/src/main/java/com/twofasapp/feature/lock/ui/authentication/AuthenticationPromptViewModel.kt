/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.lock.ui.authentication

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.android.ktx.runSafely
import com.twofasapp.core.common.ktx.decodeHex
import com.twofasapp.core.locale.Strings
import com.twofasapp.data.main.SecurityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

internal class AuthenticationPromptViewModel(
    private val securityRepository: SecurityRepository,
    private val strings: Strings,
) : ViewModel() {
    val uiState = MutableStateFlow(AuthenticationPromptUiState())

    init {
        launchScoped {
            combine(
                securityRepository.observeBiometricsEnabled(),
                securityRepository.observeMasterKeyEncryptedWithBiometrics(),
            ) { a, b ->
                Pair(a, b)
            }.collect { (biometricsEnabled, masterKeyEncryptedWithBiometrics) ->
                uiState.update {
                    it.copy(
                        initialising = false,
                        biometricsEnabled = biometricsEnabled,
                        masterKeyEncryptedWithBiometrics = masterKeyEncryptedWithBiometrics,
                    )
                }
            }
        }
    }

    fun checkPassword(password: String, onSuccess: (ByteArray) -> Unit) {
        uiState.update { it.copy(passwordError = null, loading = true) }

        launchScoped {
            runSafely { securityRepository.getMasterKeyWithPassword(password) }
                .onSuccess { masterKey ->
                    onSuccess(masterKey.decodeHex())
                }
                .onFailure {
                    uiState.update { it.copy(passwordError = strings.lockScreenUnlockInvalidPassword, loading = false) }
                }
        }
    }
}