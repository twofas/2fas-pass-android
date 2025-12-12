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
        updateItemContent { content ->
            content.copy(
                cardNumber = SecretField.ClearText(text),
                cardNumberMask = "TODO",
                cardIssuer = ItemContent.PaymentCard.Issuer.Visa,
            )
        }
    }

    fun updateExpirationDate(text: String) {
        updateItemContent { content -> content.copy(expirationDate = SecretField.ClearText(text)) }
    }

    fun updateSecurityCode(text: String) {
        updateItemContent { content -> content.copy(securityCode = SecretField.ClearText(text)) }
    }

    fun updateNotes(notes: String) {
        updateItemContent { content -> content.copy(notes = notes) }
    }
}