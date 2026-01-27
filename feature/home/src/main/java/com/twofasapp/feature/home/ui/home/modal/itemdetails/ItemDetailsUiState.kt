/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.home.modal.itemdetails

import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item

internal data class ItemDetailsUiState(
    val item: Item = Item.Empty,
    val tags: List<Tag> = emptyList(),
    val decryptedFields: Map<SecretFieldType, String> = emptyMap(),
)

internal enum class SecretFieldType {
    LoginPassword,
    SecureNote,
    PaymentCardNumber,
    PaymentCardExpiration,
    PaymentCardSecureCode,
}