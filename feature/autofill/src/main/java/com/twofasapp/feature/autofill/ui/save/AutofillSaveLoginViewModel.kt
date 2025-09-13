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
import com.twofasapp.core.common.domain.ItemUri
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.normalizeBeforeSaving
import com.twofasapp.data.main.ItemsRepository
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
    private val itemsRepository: ItemsRepository,
    private val vaultCryptoScope: VaultCryptoScope,
    private val itemEncryptionMapper: ItemEncryptionMapper,
) : ViewModel() {
    val uiState = MutableStateFlow(AutofillSaveLoginUiState())

    fun initLogin(saveLoginData: SaveLoginData) {
        launchScoped {
            val initialItem = Item.create(
                securityType = settingsRepository.observeDefaultSecurityType().first(),
                contentType = "login",
                content = ItemContent.Login.Empty.copy(
                    name = saveLoginData.uri.orEmpty(),
                    username = saveLoginData.username,
                    password = saveLoginData.password?.let { it1 -> SecretField.ClearText(it1) },
                    uris = listOfNotNull(
                        saveLoginData.uri?.let { it1 ->
                            ItemUri(
                                text = it1,
                            )
                        },
                    ),
                ),
            )

            uiState.update {
                it.copy(
                    initialItem = initialItem,
                    item = initialItem,
                )
            }
        }
    }

    fun updateItem(item: Item) {
        uiState.update { it.copy(item = item) }
    }

    fun updateIsValid(isValid: Boolean) {
        uiState.update { it.copy(isValid = isValid) }
    }

    fun save(onComplete: () -> Unit) {
        launchScoped {
            val vaultId = vaultsRepository.getVault().id
            val item = withContext(dispatchers.io) {
                itemEncryptionMapper.encryptItem(
                    item = uiState.value.item
                        .copy(vaultId = vaultId)
                        .normalizeBeforeSaving(),
                    vaultCipher = vaultCryptoScope.getVaultCipher(vaultId),
                )
            }

            itemsRepository.saveItem(item)
        }.invokeOnCompletion { onComplete() }
    }
}