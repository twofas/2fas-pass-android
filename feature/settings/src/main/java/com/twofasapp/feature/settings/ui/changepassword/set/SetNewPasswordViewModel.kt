/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.changepassword.set

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.crypto.AndroidKeyStore
import com.twofasapp.core.common.crypto.encrypt
import com.twofasapp.core.common.ktx.encodeBase64
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class SetNewPasswordViewModel(
    private val androidKeyStore: AndroidKeyStore,
) : ViewModel() {
    val uiState = MutableStateFlow(SetNewPasswordUiState())

    fun updatePassword(text: String) {
        uiState.update { it.copy(password = text) }
    }

    fun updatePasswordValidation(valid: Boolean) {
        uiState.update { it.copy(passwordValid = valid) }
    }

    fun proceed(onComplete: (String) -> Unit) {
        uiState.update { it.copy(loading = true) }

        launchScoped {
            val encryptedPassword = encrypt(androidKeyStore.appKey, uiState.value.password.toByteArray())
            uiState.update { it.copy(loading = false) }
            onComplete(encryptedPassword.encodeBase64())
        }
    }
}