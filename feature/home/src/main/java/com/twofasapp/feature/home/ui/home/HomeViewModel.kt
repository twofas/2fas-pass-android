/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.home

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.core.common.build.BuildVariant
import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.design.state.ScreenState
import com.twofasapp.core.design.state.empty
import com.twofasapp.core.design.state.success
import com.twofasapp.data.main.CloudRepository
import com.twofasapp.data.main.LoginsRepository
import com.twofasapp.data.main.TrashRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import com.twofasapp.data.purchases.PurchasesRepository
import com.twofasapp.data.purchases.domain.SubscriptionPlan
import com.twofasapp.data.settings.SessionRepository
import com.twofasapp.data.settings.SettingsRepository
import com.twofasapp.data.settings.domain.SortingMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

internal class HomeViewModel(
    appBuild: AppBuild,
    private val dispatchers: Dispatchers,
    private val settingsRepository: SettingsRepository,
    private val sessionRepository: SessionRepository,
    private val vaultsRepository: VaultsRepository,
    private val loginsRepository: LoginsRepository,
    private val trashRepository: TrashRepository,
    private val vaultCryptoScope: VaultCryptoScope,
    private val cloudRepository: CloudRepository,
    private val purchasesRepository: PurchasesRepository,
    private val itemEncryptionMapper: ItemEncryptionMapper,
) : ViewModel() {

    val uiState = MutableStateFlow(HomeUiState())
    val screenState = MutableStateFlow(ScreenState.Loading)

    init {
        uiState.update {
            it.copy(
                developerModeEnabled = when (appBuild.buildVariant) {
                    BuildVariant.Release -> false
                    BuildVariant.Internal -> true
                    BuildVariant.Debug -> true
                },
            )
        }

        launchScoped {
            settingsRepository.observeLoginClickAction().collect { action ->
                uiState.update { it.copy(loginClickAction = action) }
            }
        }

        launchScoped {
            settingsRepository.observeSortingMethod().collect { sortingMethod ->
                uiState.update { it.copy(sortingMethod = sortingMethod) }
            }
        }

        launchScoped {
            purchasesRepository.observeSubscriptionPlan().collect { plan ->
                uiState.update { it.copy(maxItems = plan.entitlements.itemsLimit) }

                when (plan) {
                    is SubscriptionPlan.Free -> Unit
                    is SubscriptionPlan.Paid -> {
                        cloudRepository.sync()
                    }
                }
            }
        }

        launchScoped {
            val vault = vaultsRepository.getVault()

            uiState.update { it.copy(vault = vault) }

            combine(
                loginsRepository.observeLogins(vaultId = vault.id),
                settingsRepository.observeSortingMethod(),
            ) { a, b -> Pair(a, b) }
                .map { (logins, sortingMethod) ->
                    vaultCryptoScope.withVaultCipher(vault) {
                        logins
                            .mapNotNull { login ->
                                val matchingLoginUiState = uiState.value.logins.find { it.id == login.id }

                                if (matchingLoginUiState?.updatedAt == login.updatedAt) {
                                    matchingLoginUiState
                                } else {
                                    itemEncryptionMapper.decryptLogin(login, this)
                                }
                            }
                            .sortedWith(
                                when (sortingMethod) {
                                    SortingMethod.NameAsc -> compareBy<Login> { it.name.lowercase() }.thenBy { it.createdAt }
                                    SortingMethod.NameDesc -> compareByDescending<Login> { it.name.lowercase() }.thenByDescending { it.createdAt }
                                    SortingMethod.CreationDateAsc -> compareBy<Login> { it.createdAt }.thenBy { it.name.lowercase() }
                                    SortingMethod.CreationDateDesc -> compareByDescending<Login> { it.createdAt }.thenByDescending { it.name.lowercase() }
                                },
                            )
                    }
                }
                .flowOn(dispatchers.io)
                .collect { logins ->
                    if (logins.isEmpty()) {
                        screenState.empty()
                    } else {
                        screenState.success()
                    }

                    uiState.update { it.copy(logins = logins) }
                }
        }
    }

    fun search(query: String) {
        uiState.update { it.copy(searchQuery = query) }
    }

    fun focusSearch(searchFocused: Boolean) {
        uiState.update { it.copy(searchFocused = searchFocused) }
    }

    fun trash(id: String) {
        launchScoped {
            trashRepository.trash(id)
        }
    }

    fun updateSortingMethod(sortingMethod: SortingMethod) {
        launchScoped { settingsRepository.setSortingMethod(sortingMethod) }
    }

    fun copyPasswordToClipboard(login: Login) {
        launchScoped {
            vaultCryptoScope.withVaultCipher(login.vaultId) {
                itemEncryptionMapper.decryptPassword(
                    login = login,
                    vaultCipher = this,
                )?.let {
                    publishEvent(HomeUiEvent.CopyPasswordToClipboard(text = it))
                }
            }
        }
    }

    fun openSettingsAndScrollToTransferSection(onComplete: () -> Unit) {
        launchScoped {
            sessionRepository.setScrollToSettingsTransferSection(true)
        }.invokeOnCompletion { onComplete() }
    }

    fun consumeEvent(event: HomeUiEvent) {
        uiState.update { it.copy(events = it.events.minus(event).distinct()) }
    }

    private fun publishEvent(event: HomeUiEvent) {
        uiState.update { it.copy(events = it.events.plus(event).distinct()) }
    }
}