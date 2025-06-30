/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.ui.auth

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.android.ktx.runSafely
import com.twofasapp.core.common.ktx.encodeHex
import com.twofasapp.data.main.LoginsRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.VaultKeysRepository
import com.twofasapp.feature.autofill.service.domain.AutofillLogin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

internal class AutofillAuthViewModel(
    private val vaultKeysRepository: VaultKeysRepository,
    private val loginsRepository: LoginsRepository,
    private val vaultCryptoScope: VaultCryptoScope,
) : ViewModel() {
    val uiState = MutableStateFlow(AutofillAuthUiState())

    private val autofillLogin: AutofillLogin
        get() = uiState.value.autofillLogin

    fun initLogin(login: AutofillLogin) {
        uiState.update { it.copy(autofillLogin = login) }
    }

    fun authenticate(masterKey: ByteArray, onSuccess: (AutofillLogin) -> Unit) {
        launchScoped {
            runSafely {
                val login = loginsRepository.getLogin(autofillLogin.id)
                val vaultKeys = vaultKeysRepository.generateVaultKeys(masterKey.encodeHex(), login.vaultId)
                vaultCryptoScope.withVaultCipher(vaultKeys) { login.password?.let { decryptWithSecretKey(it) } }
            }
                .onSuccess {
                    onSuccess(autofillLogin.copy(password = it, encrypted = false))
                }
                .onFailure {
                    Timber.e(it)
                }
        }
    }
}