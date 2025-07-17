/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.ui.save

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.common.domain.LoginUri
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.filterAndNormalizeUris
import com.twofasapp.data.main.LoginsRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import com.twofasapp.data.settings.SettingsRepository
import com.twofasapp.feature.autofill.service.domain.SaveLoginData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

internal class AutofillSaveLoginViewModel(
    private val dispatchers: Dispatchers,
    private val vaultsRepository: VaultsRepository,
    private val settingsRepository: SettingsRepository,
    private val loginsRepository: LoginsRepository,
    private val vaultCryptoScope: VaultCryptoScope,
    private val itemEncryptionMapper: ItemEncryptionMapper,
) : ViewModel() {
    val uiState = MutableStateFlow(AutofillSaveLoginUiState())

    fun initLogin(saveLoginData: SaveLoginData) {
        launchScoped {
            val initialLogin = Login.Empty.copy(
                name = saveLoginData.uri.orEmpty(),
                username = saveLoginData.username,
                password = saveLoginData.password?.let { it1 -> SecretField.Visible(it1) },
                securityType = settingsRepository.observeDefaultSecurityType().first(),
                uris = listOfNotNull(
                    saveLoginData.uri?.let { it1 ->
                        LoginUri(
                            text = it1,
                        )
                    },
                ),
            )

            uiState.update {
                it.copy(
                    initialLogin = initialLogin,
                    login = initialLogin,
                )
            }
        }
    }

    fun updateLogin(login: Login) {
        uiState.update { it.copy(login = login) }
    }

    fun updateIsValid(isValid: Boolean) {
        uiState.update { it.copy(isValid = isValid) }
    }

    fun save(onComplete: () -> Unit) {
        launchScoped {
            val vaultId = vaultsRepository.getVault().id
            val login = withContext(dispatchers.io) {
                uiState.value.login
                    .copy(
                        vaultId = vaultId,
                    )
                    .filterAndNormalizeUris()
                    .let { itemEncryptionMapper.encryptLogin(it, vaultCryptoScope.getVaultCipher(vaultId)) }
            }

            loginsRepository.saveLogin(login)
        }.invokeOnCompletion { onComplete() }
    }
}