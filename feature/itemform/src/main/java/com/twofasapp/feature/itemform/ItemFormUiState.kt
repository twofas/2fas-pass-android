/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform

import androidx.compose.runtime.Immutable
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.clearTextOrNull
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.data.main.mapper.PaymentCardValidator

/**
 * Common UI state for all item forms.
 */
@Immutable
internal data class ItemFormUiState<T : ItemContent>(
    val initialised: Boolean = false,
    val initialItem: Item = Item.Empty,
    val item: Item = Item.Empty,
    val initialItemContent: T? = null,
    val itemContent: T? = null,
    val tags: List<Tag> = emptyList(),
) {
    companion object {
        const val NOTES_LIMIT = 2048
    }

    val hasUnsavedChanges: Boolean
        get() = initialItem != item

    val valid: Boolean
        get() = when (itemContent) {
            is ItemContent.Login -> itemContent.name.isNotEmpty() && itemContent.notes.orEmpty().length <= NOTES_LIMIT
            is ItemContent.SecureNote -> itemContent.name.isNotEmpty() && itemContent.text.clearTextOrNull.orEmpty().length <= ItemContent.SecureNote.Limit
            is ItemContent.PaymentCard -> validatePaymentCard(itemContent)
            is ItemContent.Unknown -> false
            is ItemContent.Wifi -> validateWifi(itemContent)
            is ItemContent.Passkey -> false//TODO passkey
        }

    private fun validatePaymentCard(content: ItemContent.PaymentCard): Boolean {
        if (content.name.isEmpty()) return false

        if (content.notes.orEmpty().length > NOTES_LIMIT) return false

        content.cardNumber.clearTextOrNull?.takeIf { it.isNotBlank() }?.let {
            if (PaymentCardValidator.validateCardNumber(it, content.cardIssuer).not()) {
                return false
            }
        }

        content.expirationDate.clearTextOrNull?.takeIf { it.isNotBlank() }?.let {
            if (PaymentCardValidator.validateExpirationDate(it).not()) {
                return false
            }
        }

        content.securityCode.clearTextOrNull?.takeIf { it.isNotBlank() }?.let {
            if (PaymentCardValidator.validateSecurityCode(it, content.cardIssuer).not()) {
                return false
            }
        }

        return true
    }

    private fun validateWifi(content: ItemContent.Wifi): Boolean {
        return content.name.isNotEmpty()
    }
}