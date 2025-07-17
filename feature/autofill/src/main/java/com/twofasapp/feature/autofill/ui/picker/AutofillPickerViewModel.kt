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
import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.common.domain.LoginUri
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.design.state.ScreenState
import com.twofasapp.core.design.state.empty
import com.twofasapp.core.design.state.success
import com.twofasapp.data.main.LoginsRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import com.twofasapp.feature.autofill.service.domain.AutofillLogin
import com.twofasapp.feature.autofill.service.domain.AutofillLoginMatcher
import com.twofasapp.feature.autofill.service.domain.asAutofillLogin
import com.twofasapp.feature.autofill.service.parser.NodeStructure
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

internal class AutofillPickerViewModel(
    private val dispatchers: Dispatchers,
    private val loginsRepository: LoginsRepository,
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

            loginsRepository.observeLogins(vaultId = vault.id)
                .map { logins ->
                    vaultCryptoScope.withVaultCipher(vault) {
                        logins
                            .filter {
                                when (it.securityType) {
                                    SecurityType.Tier1 -> false
                                    SecurityType.Tier2 -> true
                                    SecurityType.Tier3 -> true
                                }
                            }
                            .mapNotNull { login ->
                                itemEncryptionMapper.decryptLogin(
                                    itemEncrypted = login,
                                    vaultCipher = this,
                                    decryptPassword = true,
                                )
                            }
                    }
                }
                .flowOn(dispatchers.io)
                .collect { logins ->
                    if (logins.isEmpty()) {
                        screenState.empty()
                    } else {
                        screenState.success()
                    }

                    val grouped = AutofillLoginMatcher.matchByUri(
                        logins = logins,
                        packageName = nodeStructure.packageName,
                        webDomain = nodeStructure.webDomain,
                    )

                    uiState.update {
                        it.copy(
                            suggestedLogins = grouped.filterKeys { key -> key != null }.values.flatten().sortedBy { login -> login.updatedAt },
                            otherLogins = grouped[null].orEmpty().sortedBy { login -> login.updatedAt },
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

    fun fillAndRemember(login: Login, onSuccess: (AutofillLogin) -> Unit) {
        launchScoped {
            loginsRepository.saveLogin(
                login
                    .copy(
                        uris = login.uris.plus(
                            buildList {
                                if (uiState.value.nodeStructure.webDomain.isNullOrBlank().not()) {
                                    add(
                                        LoginUri(
                                            text = "$uriPrefixWebsite${uiState.value.nodeStructure.webDomain}",
                                        ),
                                    )
                                } else {
                                    if (uiState.value.nodeStructure.packageName.isNullOrBlank().not()) {
                                        add(
                                            LoginUri(
                                                text = "$uriPrefixAndroidApp${uiState.value.nodeStructure.packageName}",
                                            ),
                                        )
                                    }
                                }
                            },
                        ).distinct(),
                    )
                    .let {
                        itemEncryptionMapper.encryptLogin(
                            login = it,
                            vaultCipher = vaultCryptoScope.getVaultCipher(vaultsRepository.getVault().id),
                        )
                    },
            )

            onSuccess(login.asAutofillLogin())
        }
    }

    fun fill(login: Login, onSuccess: (AutofillLogin) -> Unit) {
        onSuccess(login.asAutofillLogin())
    }
}