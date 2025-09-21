/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.ui.picker

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.android.ktx.uriPrefixAndroidApp
import com.twofasapp.core.android.ktx.uriPrefixWebsite
import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.domain.ItemUri
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.design.state.ScreenState
import com.twofasapp.core.design.state.empty
import com.twofasapp.core.design.state.success
import com.twofasapp.data.main.ItemsRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import com.twofasapp.feature.autofill.service.domain.AutofillItemMatcher
import com.twofasapp.feature.autofill.service.domain.AutofillLogin
import com.twofasapp.feature.autofill.service.domain.asAutofillLogin
import com.twofasapp.feature.autofill.service.parser.NodeStructure
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

internal class AutofillPickerViewModel(
    private val dispatchers: Dispatchers,
    private val itemsRepository: ItemsRepository,
    private val vaultsRepository: VaultsRepository,
    private val vaultCryptoScope: VaultCryptoScope,
    private val itemEncryptionMapper: ItemEncryptionMapper,
) : ViewModel() {
    val uiState = MutableStateFlow(AutofillPickerUiState())
    val screenState = MutableStateFlow(ScreenState.Loading)

    fun init(nodeStructure: NodeStructure) {
        uiState.update { it.copy(nodeStructure = nodeStructure) }

        launchScoped {
            val vault = vaultsRepository.getVault()

            itemsRepository.observeItems(vaultId = vault.id)
                .map { items ->
                    vaultCryptoScope.withVaultCipher(vault) {
                        items
                            .filter {
                                when (it.securityType) {
                                    SecurityType.Tier1 -> false
                                    SecurityType.Tier2 -> true
                                    SecurityType.Tier3 -> true
                                }
                            }
                            .mapNotNull { login ->
                                itemEncryptionMapper.decryptItem(
                                    itemEncrypted = login,
                                    vaultCipher = this,
                                    decryptSecretFields = true,
                                )
                            }
                    }
                }
                .flowOn(dispatchers.io)
                .collect { items ->
                    if (items.isEmpty()) {
                        screenState.empty()
                    } else {
                        screenState.success()
                    }

                    val grouped = AutofillItemMatcher.matchByUri(
                        items = items,
                        packageName = nodeStructure.packageName,
                        webDomain = nodeStructure.webDomain,
                    )

                    uiState.update {
                        it.copy(
                            suggestedItems = grouped.filterKeys { key -> key != null }.values.flatten().sortedBy { login -> login.updatedAt },
                            otherItems = grouped[null].orEmpty().sortedBy { login -> login.updatedAt },
                        )
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

    fun fillAndRemember(item: Item, onSuccess: (AutofillLogin) -> Unit) {
        launchScoped {
            when (item.content) {
                is ItemContent.Unknown -> Unit
                is ItemContent.Login -> {
                    itemsRepository.saveItem(
                        item
                            .copy(
                                content = (item.content as ItemContent.Login).copy(
                                    uris = (item.content as ItemContent.Login).uris.plus(
                                        buildList {
                                            if (uiState.value.nodeStructure.webDomain.isNullOrBlank().not()) {
                                                add(
                                                    ItemUri(
                                                        text = "$uriPrefixWebsite${uiState.value.nodeStructure.webDomain}",
                                                    ),
                                                )
                                            } else {
                                                if (uiState.value.nodeStructure.packageName.isNullOrBlank().not()) {
                                                    add(
                                                        ItemUri(
                                                            text = "$uriPrefixAndroidApp${uiState.value.nodeStructure.packageName}",
                                                        ),
                                                    )
                                                }
                                            }
                                        },
                                    ).distinct(),
                                ),
                            )
                            .let {
                                itemEncryptionMapper.encryptItem(
                                    item = it,
                                    vaultCipher = vaultCryptoScope.getVaultCipher(vaultsRepository.getVault().id),
                                )
                            },
                    )
                }

                is ItemContent.SecureNote -> Unit
            }

            item.asAutofillLogin()?.let { onSuccess(it) }
        }
    }

    fun fill(item: Item, onSuccess: (AutofillLogin) -> Unit) {
        item.asAutofillLogin()?.let { onSuccess(it) }
    }
}