/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.changepassword.current

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.data.main.SecurityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class EnterCurrentPasswordViewModel(
    private val securityRepository: SecurityRepository,
) : ViewModel() {
    val uiState = MutableStateFlow(EnterCurrentPasswordUiState())

    fun updatePassword(password: String) {
        uiState.update { it.copy(password = password) }
    }

    fun proceed(onComplete: () -> Unit) {
        uiState.update { it.copy(loading = true) }

        launchScoped {
            if (securityRepository.checkCurrentPassword(uiState.value.password)) {
                uiState.update { it.copy(loading = false) }
                onComplete()
            } else {
                uiState.update { it.copy(loading = false, error = "Invalid password") }
            }
        }
    }
}