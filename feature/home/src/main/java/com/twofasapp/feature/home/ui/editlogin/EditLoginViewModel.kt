/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.editlogin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.android.navigation.Screen
import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.common.domain.filterAndNormalizeUris
import com.twofasapp.data.main.LoginsRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import com.twofasapp.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

internal class EditLoginViewModel(
    savedStateHandle: SavedStateHandle,
    private val dispatchers: Dispatchers,
    private val loginsRepository: LoginsRepository,
    private val settingsRepository: SettingsRepository,
    private val vaultCryptoScope: VaultCryptoScope,
    private val itemEncryptionMapper: ItemEncryptionMapper,
) : ViewModel() {

    private val id: String = savedStateHandle.toRoute<Screen.EditLogin>().loginId
    private val vaultId: String = savedStateHandle.toRoute<Screen.EditLogin>().vaultId

    private val isNewLogin = id.isBlank()
    val uiState = MutableStateFlow(EditLoginUiState())

    init {
        if (isNewLogin) {
            launchScoped {
                uiState.update { state ->
                    val emptyLogin = Login.Empty.copy(securityType = settingsRepository.observeDefaultSecurityType().first())

                    state.copy(
                        initialLogin = emptyLogin,
                        login = emptyLogin,
                    )
                }
            }
        } else {
            launchScoped(dispatchers.io) {
                vaultCryptoScope.withVaultCipher(vaultId) {
                    val login = loginsRepository.getLogin(id)
                    val initialLogin = login
                        .let {
                            itemEncryptionMapper.decryptLogin(
                                itemEncrypted = it,
                                vaultCipher = this,
                                decryptPassword = true,
                            )
                        } ?: return@withVaultCipher

                    uiState.update { state ->
                        state.copy(
                            initialLogin = initialLogin,
                            login = initialLogin,
                        )
                    }
                }
            }
        }
    }

    fun updateLogin(login: Login) {
        uiState.update { it.copy(login = login) }
    }

    fun updateIsValid(isValid: Boolean) {
        uiState.update { it.copy(isValid = isValid) }
    }

    fun updateHasUnsavedChanges(hasUnsavedChanges: Boolean) {
        uiState.update { it.copy(hasUnsavedChanges = hasUnsavedChanges) }
    }

    fun save(onComplete: () -> Unit) {
        launchScoped {
            val login = withContext(dispatchers.io) {
                uiState.value.login
                    .copy(
                        vaultId = vaultId,
                    )
                    .filterAndNormalizeUris()
                    .let {
                        itemEncryptionMapper.encryptLogin(
                            login = it,
                            vaultCipher = vaultCryptoScope.getVaultCipher(vaultId),
                        )
                    }
            }

            loginsRepository.saveLogin(login)
        }.invokeOnCompletion { onComplete() }
    }
}