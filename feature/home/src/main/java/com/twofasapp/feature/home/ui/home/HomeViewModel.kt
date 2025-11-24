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
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.ktx.toggle
import com.twofasapp.core.design.state.ScreenState
import com.twofasapp.core.design.state.empty
import com.twofasapp.core.design.state.loading
import com.twofasapp.core.design.state.success
import com.twofasapp.data.main.CloudRepository
import com.twofasapp.data.main.ItemsRepository
import com.twofasapp.data.main.TagsRepository
import com.twofasapp.data.main.TrashRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import com.twofasapp.data.purchases.PurchasesRepository
import com.twofasapp.data.purchases.domain.SubscriptionPlan
import com.twofasapp.data.settings.SessionRepository
import com.twofasapp.data.settings.SettingsRepository
import com.twofasapp.data.settings.domain.SortingMethod
import kotlinx.coroutines.delay
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
    private val itemsRepository: ItemsRepository,
    private val tagsRepository: TagsRepository,
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
            settingsRepository.observeItemClickAction().collect { action ->
                uiState.update { it.copy(itemClickAction = action) }
            }
        }

        launchScoped {
            settingsRepository.observeSortingMethod().collect { sortingMethod ->
                uiState.update { it.copy(sortingMethod = sortingMethod) }
            }
        }

        launchScoped {
            tagsRepository.observeSelectedTag(vaultId = vaultsRepository.getVault().id).collect { selectedTag ->
                uiState.update { it.copy(selectedTag = selectedTag) }
            }
        }

        launchScoped {
            tagsRepository.observeTags(vaultId = vaultsRepository.getVault().id).collect { tags ->
                uiState.update { it.copy(tags = tags) }
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
                itemsRepository.observeItems(vaultId = vault.id),
                settingsRepository.observeSortingMethod(),
            ) { a, b -> Pair(a, b) }
                .map { (items, sortingMethod) ->
                    vaultCryptoScope.withVaultCipher(vault) {
                        items
                            .mapNotNull { item ->
                                val matchingItemUiState = uiState.value.items.find { it.id == item.id }

                                if (matchingItemUiState?.updatedAt == item.updatedAt) {
                                    matchingItemUiState
                                } else {
                                    itemEncryptionMapper.decryptItem(item, this)
                                }
                            }
                            .sortedWith(
                                when (sortingMethod) {
                                    SortingMethod.NameAsc -> compareBy<Item> { it.content.name.lowercase() }.thenBy { it.createdAt }
                                    SortingMethod.NameDesc -> compareByDescending<Item> { it.content.name.lowercase() }.thenByDescending { it.createdAt }
                                    SortingMethod.CreationDateAsc -> compareBy<Item> { it.createdAt }.thenBy { it.content.name.lowercase() }
                                    SortingMethod.CreationDateDesc -> compareByDescending<Item> { it.createdAt }.thenByDescending { it.content.name.lowercase() }
                                },
                            )
                    }
                }
                .flowOn(dispatchers.io)
                .collect { items ->
                    if (items.isEmpty()) {
                        screenState.empty()
                    } else {
                        screenState.success()
                    }

                    uiState.update { it.copy(items = items) }
                }
        }

        launchScoped {
            sessionRepository.observeQuickSetupPrompted().collect { quickSetupPrompted ->
                if (quickSetupPrompted.not()) {
                    delay(500)
                    publishEvent(HomeUiEvent.OpenQuickSetup)
                }
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

    fun toggleTag(tag: Tag) {
        launchScoped { tagsRepository.toggleSelectedTag(uiState.value.vault.id, tag) }
    }

    fun clearFilters() {
        launchScoped {
            tagsRepository.clearSelectedTag(uiState.value.vault.id)
        }
    }

    fun decryptSecretField(
        item: Item,
        secretField: SecretField?,
        onDecrypted: (String) -> Unit,
    ) {
        if (secretField == null) {
            return
        }

        launchScoped {
            vaultCryptoScope.withVaultCipher(item.vaultId) {
                itemEncryptionMapper.decryptSecretField(
                    secretField = secretField,
                    securityType = item.securityType,
                    vaultCipher = this,
                )?.let { onDecrypted(it) }
            }
        }
    }

    fun consumeEvent(event: HomeUiEvent) {
        uiState.update { it.copy(events = it.events.minus(event).distinct()) }
    }

    private fun publishEvent(event: HomeUiEvent) {
        uiState.update { it.copy(events = it.events.plus(event).distinct()) }
    }

    fun changeEditMode(enabled: Boolean) {
        uiState.update { it.copy(editMode = enabled) }

        if (enabled.not()) {
            uiState.update { it.copy(selectedItemIds = emptyList()) }
        }
    }

    fun toggleItemSelection(itemId: String) {
        uiState.update { state ->
            val selectedItemIds = state.selectedItemIds.toggle(itemId)

            state.copy(
                selectedItemIds = selectedItemIds,
                editMode = selectedItemIds.isNotEmpty(),
            )
        }
    }

    fun selectAllItems() {
        uiState.update { state ->
            state.copy(
                selectedItemIds = state.itemsFiltered.map { it.id },
            )
        }
    }

    fun deselectItems() {
        uiState.update { state ->
            state.copy(
                selectedItemIds = emptyList(),
            )
        }
    }

    private fun cleatEditModeSelections() {
        uiState.update { it.copy(selectedItemIds = emptyList(), editMode = false) }
    }

    fun trashSelectedItems() {
        val idsToDelete = uiState.value.selectedItemIds
        cleatEditModeSelections()

        screenState.loading()

        launchScoped {
            trashRepository.trash(*idsToDelete.toTypedArray())
        }
    }

    fun changeSelectedItemsSecurityType(securityType: SecurityType) {
        launchScoped {
            val itemsToEdit = uiState.value.selectedItems.filter { it.securityType != securityType }
            cleatEditModeSelections()
            screenState.loading()

            val updatedEncryptedItems = vaultCryptoScope.withVaultCipher(vaultId = vaultsRepository.getVault().id) {
                val updatedItems = itemsToEdit.map { item ->
                    item.copy(
                        securityType = securityType,
                        content = itemEncryptionMapper.decryptSecretFields(this, item.securityType, item.content),
                    )
                }

                itemEncryptionMapper.encryptItems(
                    vaultCipher = this,
                    items = updatedItems,
                )
            }

            itemsRepository.saveItems(updatedEncryptedItems)

            publishEvent(HomeUiEvent.ShowToast("Items updated!"))
        }
    }

    fun changeSelectedItemsTags(changedTags: Map<Item, Set<String>>) {
        launchScoped {
            cleatEditModeSelections()
            screenState.loading()

            itemsRepository.updateItemsWithTags(changedTags)

            publishEvent(HomeUiEvent.ShowToast("Items updated!"))
        }
    }
}