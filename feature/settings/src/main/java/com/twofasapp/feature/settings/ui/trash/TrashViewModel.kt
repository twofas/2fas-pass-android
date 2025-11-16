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
import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.design.state.ScreenState
import com.twofasapp.core.design.state.empty
import com.twofasapp.core.design.state.loading
import com.twofasapp.core.design.state.success
import com.twofasapp.data.main.ItemsRepository
import com.twofasapp.data.main.TrashRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import com.twofasapp.data.purchases.PurchasesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class TrashViewModel(
    dispatchers: Dispatchers,
    private val purchasesRepository: PurchasesRepository,
    private val trashRepository: TrashRepository,
    private val itemsRepository: ItemsRepository,
    private val vaultCryptoScope: VaultCryptoScope,
    private val itemEncryptionMapper: ItemEncryptionMapper,
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
            itemsRepository.getItemsCount().let { count ->
                uiState.update { it.copy(itemsCount = count) }
            }
        }

        launchScoped(dispatchers.io) {
            trashRepository.observeDeleted().collect { items ->
                val itemStates = items
                    .groupBy { it.vaultId }
                    .map { (vaultId, items) ->
                        vaultCryptoScope.withVaultCipher(vaultId) {
                            items.map { item ->
                                val matchingItemUiState = uiState.value.trashedItems.find { it.id == item.id }

                                if (matchingItemUiState?.updatedAt == item.updatedAt) {
                                    matchingItemUiState
                                } else {
                                    itemEncryptionMapper.decryptItem(item, this)
                                }
                            }
                        }
                    }
                    .flatten()
                    .filterNotNull()
                    .sortedByDescending { it.updatedAt }

                uiState.update { it.copy(trashedItems = itemStates) }

                if (items.isEmpty()) {
                    screenState.empty("List is empty")
                } else {
                    screenState.success()
                }
            }
        }
    }

    fun restore(onComplete: (String) -> Unit) {
        launchScoped {
            val ids = uiState.value.selected.toTypedArray()
            screenState.loading()
            clearSelections()
            trashRepository.restore(*ids)
        }.invokeOnCompletion { onComplete("Items restored!") }
    }

    fun delete(onComplete: (String) -> Unit) {
        launchScoped {
            val ids = uiState.value.selected.toTypedArray()
            screenState.loading()
            clearSelections()
            trashRepository.delete(*ids)
        }.invokeOnCompletion { onComplete("Items deleted!") }
    }

    fun toggle(item: Item) {
        if (uiState.value.selected.contains(item.id)) {
            uiState.update { it.copy(selected = it.selected.minus(item.id)) }
        } else {
            uiState.update { it.copy(selected = it.selected.plus(item.id)) }
        }
    }

    fun selectAll() {
        uiState.update { it.copy(selected = it.trashedItems.map { login -> login.id }) }
    }

    fun clearSelections() {
        uiState.update { it.copy(selected = emptyList()) }
    }
}