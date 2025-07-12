/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.service.domain

import com.twofasapp.core.common.domain.ItemEncrypted
import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.data.main.LoginsRepository
import com.twofasapp.data.main.VaultCryptoScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal object AutofillLoginMatcher {

    suspend fun matchByUri(
        loginsRepository: LoginsRepository,
        vaultCryptoScope: VaultCryptoScope,
        logins: List<ItemEncrypted>,
        packageName: String?,
        webDomain: String?,
    ): List<AutofillLogin> {
        val uriSearchTerms = listOfNotNull(
            webDomain?.lowercase()?.trim()?.sanitized,
            packageName?.lowercase()?.trim()?.sanitized,
        )

        return withContext(Dispatchers.IO) {
            logins.asSequence()
                .filter {
                    when (it.securityType) {
                        SecurityType.Tier1 -> false
                        SecurityType.Tier2 -> true
                        SecurityType.Tier3 -> true
                    }
                }
                .groupBy { it.vaultId }
                .mapNotNull { (vaultId, encryptedLogins) ->
                    val vaultCipher = vaultCryptoScope.getVaultCipher(vaultId)

                    loginsRepository.decrypt(
                        vaultCipher = vaultCipher,
                        itemsEncrypted = encryptedLogins,
                        decryptPassword = true,
                    ).map { it.asSecretAutofillLogin() }
                }
                .flatten()
                .map { autofillLogin ->
                    val matchRank = autofillLogin.uris.matchesAny(searchTerms = uriSearchTerms)

                    if (matchRank != null) {
                        autofillLogin.copy(matchRank = matchRank)
                    } else {
                        autofillLogin
                    }
                }
                .sortedWith(compareBy<AutofillLogin, Int?>(nullsLast()) { it.matchRank }.thenBy { it.updatedAt })
        }
    }

    suspend fun matchByUri(
        logins: List<Login>,
        packageName: String?,
        webDomain: String?,
    ): Map<Int?, List<Login>> {
        val uriSearchTerms = listOfNotNull(
            webDomain?.lowercase()?.trim()?.sanitized,
            packageName?.lowercase()?.trim()?.sanitized,
        )

        return withContext(Dispatchers.IO) {
            logins
                .filter {
                    when (it.securityType) {
                        SecurityType.Tier1 -> false
                        SecurityType.Tier2 -> true
                        SecurityType.Tier3 -> true
                    }
                }
                .groupBy { login ->
                    login.uris.map { it.text }.matchesAny(searchTerms = uriSearchTerms)
                }
                .toSortedMap(compareBy(nullsLast()) { it })
        }
    }

    private fun List<String>.matchesAny(searchTerms: List<String>): Int? {
        return when {
            any { uri -> searchTerms.any { term -> uri.sanitized.equals(term, true) } } -> 1
            any { uri -> searchTerms.any { term -> uri.sanitized.contains(term, true) } } -> 2
            else -> null
        }
    }

    private val String.sanitized: String
        get() =
            lowercase()
                .trim()
                .replace("http://", "")
                .replace("https://", "")
                .replace("www.", "")
                .replace("androidapp//", "")
                .removeSuffix("/")
}