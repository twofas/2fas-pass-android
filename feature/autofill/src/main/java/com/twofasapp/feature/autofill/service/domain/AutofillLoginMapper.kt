/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.service.domain

import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent

internal fun Item.asSecretAutofillLogin(): AutofillLogin? {
    return content.let { content ->
        when (content) {
            is ItemContent.Unknown -> null
            is ItemContent.CreditCard -> null
            is ItemContent.Login -> {
                AutofillLogin(
                    encrypted = when (securityType) {
                        SecurityType.Tier1 -> true
                        SecurityType.Tier2 -> true
                        SecurityType.Tier3 -> false
                    },
                    updatedAt = 0L,
                    matchRank = null,
                    id = id,
                    name = when (securityType) {
                        SecurityType.Tier1 -> null
                        SecurityType.Tier2 -> content.name
                        SecurityType.Tier3 -> content.name
                    },
                    username = when (securityType) {
                        SecurityType.Tier1 -> null
                        SecurityType.Tier2 -> content.username
                        SecurityType.Tier3 -> content.username
                    },
                    password = content.password?.let {
                        when (securityType) {
                            SecurityType.Tier1 -> null
                            SecurityType.Tier2 -> null
                            SecurityType.Tier3 -> content.password?.let { (it as? SecretField.ClearText)?.value }
                        }
                    },
                    uris = content.uris.mapNotNull { loginUri ->
                        when (securityType) {
                            SecurityType.Tier1 -> null
                            SecurityType.Tier2 -> loginUri.text
                            SecurityType.Tier3 -> loginUri.text
                        }
                    },
                )
            }

            is ItemContent.SecureNote -> null
        }
    }
}

internal fun Item.asAutofillLogin(): AutofillLogin? {
    return content.let { content ->
        when (content) {
            is ItemContent.Unknown -> null
            is ItemContent.CreditCard -> null
            is ItemContent.Login -> {
                AutofillLogin(
                    encrypted = false,
                    matchRank = null,
                    id = id,
                    name = content.name,
                    username = content.username,
                    password = content.password?.let { (it as SecretField.ClearText).value },
                    uris = content.uris.map { loginUri -> loginUri.text },
                    updatedAt = updatedAt,
                )
            }

            is ItemContent.SecureNote -> null
        }
    }
}