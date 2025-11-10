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
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.ktx.encodeHex
import com.twofasapp.data.main.ItemsRepository
import com.twofasapp.data.main.VaultKeysRepository
import com.twofasapp.feature.autofill.service.domain.AutofillLogin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

internal class AutofillAuthViewModel(
    private val vaultKeysRepository: VaultKeysRepository,
    private val itemsRepository: ItemsRepository,
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
                val loginEncrypted = itemsRepository.getItem(autofillLogin.id)
                vaultKeysRepository.generateVaultKeys(masterKey.encodeHex(), loginEncrypted.vaultId)
                itemsRepository.decrypt(itemEncrypted = loginEncrypted, decryptSecretFields = true)
            }
                .onSuccess { item ->
                    val autofillLogin = item?.content?.let { content ->
                        when (content) {
                            is ItemContent.Unknown -> autofillLogin // TODO: Migrate
                            is ItemContent.Login -> autofillLogin.copy(
                                password = (content.password as? SecretField.ClearText)?.value.orEmpty(),
                            )

                            is ItemContent.SecureNote -> autofillLogin // TODO: Migrate
                            is ItemContent.CreditCard -> autofillLogin // TODO: Migrate
                        }
                    } ?: autofillLogin // TODO: Migrate

                    onSuccess(autofillLogin.copy(encrypted = false))
                }
                .onFailure {
                    Timber.e(it)
                }
        }
    }
}