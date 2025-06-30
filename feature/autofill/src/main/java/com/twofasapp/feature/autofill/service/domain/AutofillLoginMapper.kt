/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.service.domain

import com.twofasapp.core.common.domain.EncryptedLogin
import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.common.domain.LoginSecurityType
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.data.main.VaultCipher

internal fun EncryptedLogin.asAutofillLogin(vaultCipher: VaultCipher): AutofillLogin {
    return AutofillLogin(
        encrypted = when (securityType) {
            LoginSecurityType.Tier1 -> true
            LoginSecurityType.Tier2 -> true
            LoginSecurityType.Tier3 -> false
        },
        matchRank = null,
        id = id,
        name = when (securityType) {
            LoginSecurityType.Tier1 -> null
            LoginSecurityType.Tier2 -> vaultCipher.decryptWithTrustedKey(name)
            LoginSecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(name)
        },
        username = when (securityType) {
            LoginSecurityType.Tier1 -> null
            LoginSecurityType.Tier2 -> username?.let { vaultCipher.decryptWithTrustedKey(it) }
            LoginSecurityType.Tier3 -> username?.let { vaultCipher.decryptWithTrustedKey(it) }
        },
        password = password?.let {
            when (securityType) {
                LoginSecurityType.Tier1 -> null
                LoginSecurityType.Tier2 -> null
                LoginSecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(it)
            }
        },
        uris = uris.mapNotNull { loginUri ->
            when (securityType) {
                LoginSecurityType.Tier1 -> null
                LoginSecurityType.Tier2 -> vaultCipher.decryptWithTrustedKey(loginUri.text)
                LoginSecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(loginUri.text)
            }
        },
        updatedAt = updatedAt,
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