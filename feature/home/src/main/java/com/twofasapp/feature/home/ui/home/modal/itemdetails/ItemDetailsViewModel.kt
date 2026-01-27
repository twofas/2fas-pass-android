/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.home.modal.itemdetails

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.cardNumberGrouping
import com.twofasapp.core.common.domain.items.formatWithGrouping
import com.twofasapp.core.common.ktx.removeWhitespace
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class ItemDetailsViewModel(
    private val vaultCryptoScope: VaultCryptoScope,
    private val itemEncryptionMapper: ItemEncryptionMapper,
) : ViewModel() {

    val uiState = MutableStateFlow(ItemDetailsUiState())

    fun init(item: Item, tags: List<Tag>) {
        uiState.update { it.copy(item = item, tags = tags) }

        // Auto-decrypt SecureNote for Tier3 security
        if (item.content is ItemContent.SecureNote && item.securityType == SecurityType.Tier3) {
            decryptField(SecretFieldType.SecureNote)
        }
    }

    fun toggleFieldVisibility(fieldType: SecretFieldType) {
        val currentValue = uiState.value.decryptedFields[fieldType]

        if (currentValue != null) {
            // Hide the field
            uiState.update { state ->
                state.copy(decryptedFields = state.decryptedFields - fieldType)
            }
        } else {
            // Decrypt and show the field
            decryptField(fieldType)
        }
    }

    private fun decryptField(fieldType: SecretFieldType) {
        val item = uiState.value.item
        val secretField = getSecretFieldForType(fieldType, item.content) ?: return

        launchScoped(Dispatchers.IO) {
            vaultCryptoScope.withVaultCipher(item.vaultId) {
                itemEncryptionMapper.decryptSecretField(
                    secretField = secretField,
                    securityType = item.securityType,
                    vaultCipher = this,
                )?.let { decrypted ->
                    val formattedValue = formatDecryptedValue(fieldType, decrypted, item.content)
                    uiState.update { state ->
                        state.copy(
                            decryptedFields = state.decryptedFields + (fieldType to formattedValue),
                        )
                    }
                }
            }
        }
    }

    fun clearDecryptedFields() {
        uiState.update { it.copy(decryptedFields = emptyMap()) }
    }

    private fun getSecretFieldForType(fieldType: SecretFieldType, content: ItemContent): SecretField? {
        return when (fieldType) {
            SecretFieldType.LoginPassword -> (content as? ItemContent.Login)?.password
            SecretFieldType.SecureNote -> (content as? ItemContent.SecureNote)?.text
            SecretFieldType.PaymentCardNumber -> (content as? ItemContent.PaymentCard)?.cardNumber
            SecretFieldType.PaymentCardExpiration -> (content as? ItemContent.PaymentCard)?.expirationDate
            SecretFieldType.PaymentCardSecureCode -> (content as? ItemContent.PaymentCard)?.securityCode
        }
    }

    private fun formatDecryptedValue(fieldType: SecretFieldType, decrypted: String, content: ItemContent): String {
        return when (fieldType) {
            SecretFieldType.PaymentCardNumber -> {
                val issuer = (content as? ItemContent.PaymentCard)?.cardIssuer
                decrypted.removeWhitespace().formatWithGrouping(issuer.cardNumberGrouping())
            }
            SecretFieldType.PaymentCardExpiration, SecretFieldType.PaymentCardSecureCode -> {
                decrypted.removeWhitespace()
            }
            else -> decrypted
        }
    }
}