/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.trash

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.design.state.ScreenState
import com.twofasapp.core.design.state.empty
import com.twofasapp.core.design.state.success
import com.twofasapp.data.main.LoginsRepository
import com.twofasapp.data.main.TrashRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.mapper.LoginEncryptionMapper
import com.twofasapp.data.purchases.PurchasesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class TrashViewModel(
    private val purchasesRepository: PurchasesRepository,
    private val trashRepository: TrashRepository,
    private val loginsRepository: LoginsRepository,
    private val vaultCryptoScope: VaultCryptoScope,
    private val loginEncryptionMapper: LoginEncryptionMapper,
) : ViewModel() {
    val uiState = MutableStateFlow(TrashUiState())
    val screenState = MutableStateFlow(ScreenState.Loading)

    init {
        launchScoped {
            purchasesRepository.observeSubscriptionPlan().collect { plan ->
                uiState.update { it.copy(maxItems = plan.entitlements.itemsLimit) }
            }
        }

        launchScoped {
            loginsRepository.getLoginsCount().let { count ->
                uiState.update { it.copy(loginsCount = count) }
            }
        }

        launchScoped {
            trashRepository.observeDeleted().collect { logins ->
                val loginStates = logins
                    .groupBy { it.vaultId }
                    .map { (vaultId, logins) ->
                        vaultCryptoScope.withVaultCipher(vaultId) {
                            logins.map { login ->
                                val matchingLoginUiState = uiState.value.trashedLogins.find { it.id == login.id }

                                if (matchingLoginUiState?.updatedAt == login.updatedAt) {
                                    matchingLoginUiState
                                } else {
                                    loginEncryptionMapper.decryptLogin(login, this)
                                }
                            }
                        }
                    }
                    .flatten()
                    .filterNotNull()
                    .sortedByDescending { it.updatedAt }

                uiState.update { it.copy(trashedLogins = loginStates) }

                if (logins.isEmpty()) {
                    screenState.empty("List is empty")
                } else {
                    screenState.success()
                }
            }
        }
    }

    fun restore() {
        launchScoped {
            trashRepository.restore(*uiState.value.selected.toTypedArray())
            clearSelections()
        }
    }

    fun delete() {
        launchScoped {
            trashRepository.delete(*uiState.value.selected.toTypedArray())
            clearSelections()
        }
    }

    fun toggle(login: Login) {
        if (uiState.value.selected.contains(login.id)) {
            uiState.update { it.copy(selected = it.selected.minus(login.id)) }
        } else {
            uiState.update { it.copy(selected = it.selected.plus(login.id)) }
        }
    }

    fun selectAll() {
        uiState.update { it.copy(selected = it.trashedLogins.map { login -> login.id }) }
    }

    fun clearSelections() {
        uiState.update { it.copy(selected = emptyList()) }
    }
}