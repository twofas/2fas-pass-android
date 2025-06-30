/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.createmasterpassword

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.domain.crypto.KdfSpec
import com.twofasapp.data.main.SecurityRepository
import com.twofasapp.feature.startup.ui.StartupConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class CreateMasterPasswordViewModel(
    private val securityRepository: SecurityRepository,
    private val startupConfig: StartupConfig,
) : ViewModel() {

    val uiState = MutableStateFlow(CreateMasterPasswordUiState())

    fun updatePassword(text: String) {
        uiState.update { it.copy(password = text) }
    }

    fun updatePasswordValidation(valid: Boolean) {
        uiState.update { it.copy(passwordValid = valid) }
    }

    fun generateMasterKey() {
        updateLoading(true)

        launchScoped {
            val masterKey = securityRepository.generateMasterKeyOnFirstLaunch(
                password = uiState.value.password,
                seed = startupConfig.seed!!,
                kdfSpec = KdfSpec.Argon2id(),
            )

            startupConfig.masterKey = masterKey
            updateLoading(false)
            publishEvent(CreateMasterPasswordUiEvent.Complete)
        }
    }

    fun consumeEvent(event: CreateMasterPasswordUiEvent) {
        uiState.update { state -> state.copy(events = state.events.minus(event)) }
    }

    private fun publishEvent(event: CreateMasterPasswordUiEvent) {
        uiState.update { state -> state.copy(events = state.events.plus(event)) }
    }

    private fun updateLoading(loading: Boolean) {
        uiState.update { it.copy(loading = loading) }
    }
}