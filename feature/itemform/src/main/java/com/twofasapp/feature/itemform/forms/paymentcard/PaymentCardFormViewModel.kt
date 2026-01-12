/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform.forms.paymentcard

import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.data.main.TagsRepository
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.main.mapper.PaymentCardValidator
import com.twofasapp.data.settings.SettingsRepository
import com.twofasapp.feature.itemform.ItemFormViewModel

internal class PaymentCardFormViewModel(
    vaultsRepository: VaultsRepository,
    settingsRepository: SettingsRepository,
    tagsRepository: TagsRepository,
) : ItemFormViewModel<ItemContent.PaymentCard>(
    vaultsRepository = vaultsRepository,
    settingsRepository = settingsRepository,
    tagsRepository = tagsRepository,
) {
    fun updateName(text: String) {
        updateItemContent { content -> content.copy(name = text) }
    }

    fun updateCardHolder(text: String) {
        updateItemContent { content -> content.copy(cardHolder = text) }
    }

    fun updateCardNumber(text: String) {
        val issuer = PaymentCardValidator.detectCardIssuer(text)
        val mask = PaymentCardValidator.cardNumberMask(text)

        updateItemContent { content ->
            content.copy(
                cardNumber = SecretField.ClearText(text),
                cardNumberMask = mask,
                cardIssuer = issuer,
            )
        }
    }

    fun updateExpirationDate(text: String) {
        val formatted = formatExpirationDate(text)
        updateItemContent { content -> content.copy(expirationDate = SecretField.ClearText(formatted)) }
    }

    private fun formatExpirationDate(input: String): String {
        // Input comes as digits only (e.g., "0125")
        // Store as MM/YY format (e.g., "01/25")
        return when (input.length) {
            0, 1, 2 -> input
            3 -> "${input.take(2)}/${input.substring(2)}"
            else -> "${input.take(2)}/${input.substring(2, 4)}"
        }
    }

    fun updateSecurityCode(text: String) {
        updateItemContent { content -> content.copy(securityCode = SecretField.ClearText(text)) }
    }

    fun updateNotes(notes: String) {
        updateItemContent { content -> content.copy(notes = notes) }
    }
}