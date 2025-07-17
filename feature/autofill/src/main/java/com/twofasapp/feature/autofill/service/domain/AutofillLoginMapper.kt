/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.service.domain

import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.SecurityType

internal fun Login.asSecretAutofillLogin(): AutofillLogin {
    return AutofillLogin(
        encrypted = when (securityType) {
            SecurityType.Tier1 -> true
            SecurityType.Tier2 -> true
            SecurityType.Tier3 -> false
        },
        updatedAt = 0,
        matchRank = null,
        id = id,
        name = when (securityType) {
            SecurityType.Tier1 -> null
            SecurityType.Tier2 -> name
            SecurityType.Tier3 -> name
        },
        username = when (securityType) {
            SecurityType.Tier1 -> null
            SecurityType.Tier2 -> username
            SecurityType.Tier3 -> username
        },
        password = password?.let {
            when (securityType) {
                SecurityType.Tier1 -> null
                SecurityType.Tier2 -> null
                SecurityType.Tier3 -> password?.let { (it as? SecretField.Visible)?.value }
            }
        },
        uris = uris.mapNotNull { loginUri ->
            when (securityType) {
                SecurityType.Tier1 -> null
                SecurityType.Tier2 -> loginUri.text
                SecurityType.Tier3 -> loginUri.text
            }
        },
    )
}

internal fun Login.asAutofillLogin(): AutofillLogin {
    return AutofillLogin(
        encrypted = false,
        matchRank = null,
        id = id,
        name = name,
        username = username,
        password = password?.let { (it as SecretField.Visible).value },
        uris = uris.map { loginUri -> loginUri.text },
        updatedAt = updatedAt,
    )
}