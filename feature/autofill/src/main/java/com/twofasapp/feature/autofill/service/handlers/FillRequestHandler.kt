/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.service.handlers

import android.content.Context
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import com.twofasapp.data.main.ItemsRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.settings.SettingsRepository
import com.twofasapp.feature.autofill.service.PassAutofillService.Companion.AutofillTag
import com.twofasapp.feature.autofill.service.builders.FillResponseBuilder
import com.twofasapp.feature.autofill.service.domain.AutofillItemMatcher
import com.twofasapp.feature.autofill.service.domain.FillRequestSpec
import com.twofasapp.feature.autofill.service.parser.NodeParser
import kotlinx.coroutines.flow.first
import timber.log.Timber

internal class FillRequestHandler(
    private val itemsRepository: ItemsRepository,
    private val vaultsRepository: VaultsRepository,
    private val settingsRepository: SettingsRepository,
    private val vaultCryptoScope: VaultCryptoScope,
) {
    suspend fun handleRequest(
        context: Context,
        fillRequest: FillRequest,
        fillCallback: FillCallback,
    ) {
        try {
            val nodeStructure = NodeParser().parse(fillRequest)

            if (nodeStructure.inputs.isEmpty()) {
                Timber.tag(AutofillTag).d("❌ No autofill inputs found!")
                fillCallback.onSuccess(null)
                return
            }

            if (nodeStructure.packageName == context.packageName) {
                Timber.tag(AutofillTag).d("❌ Package name is the same as autofill service package name!")
                fillCallback.onSuccess(null)
                return
            }

            Timber.tag(AutofillTag).d("✅ Node structure parsed: \n$nodeStructure")

            val fillRequestSpec = getFillRequestSpec(fillRequest)
            val loginsToTake = if (fillRequestSpec.inlinePresentationEnabled) {
                fillRequestSpec.maxItemsCount - 2 // Make room for App item and pinned item
            } else {
                fillRequestSpec.maxItemsCount - 1
            }

            val response = FillResponseBuilder.create(
                context = context,
                fillRequestSpec = fillRequestSpec,
                nodeStructure = nodeStructure,
                logins = when (fillRequestSpec.authenticated) {
                    true -> {
                        AutofillItemMatcher.matchByUri(
                            itemsRepository = itemsRepository,
                            vaultCryptoScope = vaultCryptoScope,
                            items = itemsRepository.getItems(),
                            packageName = nodeStructure.packageName,
                            webDomain = nodeStructure.webDomain,
                        )
                            .filter { it.matchRank != null }
                            .take(loginsToTake)
                    }

                    false -> {
                        emptyList()
                    }
                },
            )

            fillCallback.onSuccess(response)
        } catch (e: Exception) {
            Timber.tag(AutofillTag).e(e)
            fillCallback.onFailure("Exception when filling autofill - ${e.message}")
        }
    }

    private suspend fun getFillRequestSpec(fillRequest: FillRequest): FillRequestSpec {
        val authenticated = vaultCryptoScope.getVaultCipher(vaultsRepository.getVault().id).isTrustedValid()
        val inlineEnabled = settingsRepository.observeAutofillSettings().first().useInlinePresentation
        val inlineAvailable = fillRequest.inlineSuggestionsRequest != null
        val inlinePresentationEnabled = inlineAvailable && inlineEnabled

        return FillRequestSpec(
            autofillSessionId = fillRequest.id,
            authenticated = authenticated,
            maxItemsCount = fillRequest.inlineSuggestionsRequest?.maxSuggestionCount ?: 8,
            inlinePresentationEnabled = inlinePresentationEnabled,
            inlinePresentationSpecs = if (inlinePresentationEnabled) {
                fillRequest.inlineSuggestionsRequest?.inlinePresentationSpecs.orEmpty()
            } else {
                emptyList()
            },
            flags = fillRequest.flags,
        )
    }
}