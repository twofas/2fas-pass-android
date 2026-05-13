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
import com.twofasapp.core.common.logger.Flog
import com.twofasapp.data.main.ItemsRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.settings.SettingsRepository
import com.twofasapp.feature.autofill.service.PassAutofillService.Companion.AutofillTag
import com.twofasapp.feature.autofill.service.builders.FillResponseBuilder
import com.twofasapp.feature.autofill.service.builders.SkippedPackages
import com.twofasapp.feature.autofill.service.domain.AutofillItemMatcher
import com.twofasapp.feature.autofill.service.domain.FillRequestSpec
import com.twofasapp.feature.autofill.service.parser.NodeParser
import kotlinx.coroutines.flow.first

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
                Flog.tag(AutofillTag).d("❌ No autofill inputs found!")
                Flog.persist(tag = "Autofill", message = "FillRequest: No inputs found")
                fillCallback.onSuccess(null)
                return
            }

            if (nodeStructure.packageName.orEmpty().startsWith("com.twofasapp.pass")) {
                Flog.tag(AutofillTag).d("❌ Package name is the same as autofill service package name!")
                Flog.persist(tag = "Autofill", message = "FillRequest: Skipped own package")
                fillCallback.onSuccess(null)
                return
            }

            if (SkippedPackages.isSkipped(nodeStructure.packageName)) {
                Flog.tag(AutofillTag).d("❌ Package name is in the skipped packages list!")
                Flog.persist(tag = "Autofill", message = "FillRequest: Skipped package ${nodeStructure.packageName}")
                fillCallback.onSuccess(null)
                return
            }

            Flog.tag(AutofillTag).d("✅ Node structure parsed: \n$nodeStructure")
            Flog.persist(tag = "Autofill", message = "FillRequest: Parsed pkg=${nodeStructure.packageName} web=${nodeStructure.webDomain} inputs=${nodeStructure.inputs.size}")

            val fillRequestSpec = getFillRequestSpec(fillRequest)
            val itemsToTake = if (fillRequestSpec.inlinePresentationEnabled) {
                fillRequestSpec.maxItemsCount - 2 // Make room for App item and pinned item
            } else {
                fillRequestSpec.maxItemsCount - 1
            }

            val response = FillResponseBuilder.create(
                context = context,
                fillRequestSpec = fillRequestSpec,
                nodeStructure = nodeStructure,
                items = when (fillRequestSpec.authenticated) {
                    true -> {
                        AutofillItemMatcher.matchByUri(
                            itemsRepository = itemsRepository,
                            vaultCryptoScope = vaultCryptoScope,
                            items = itemsRepository.getItems().filter { it.contentType.fillable },
                            packageName = nodeStructure.packageName,
                            webDomain = nodeStructure.webDomain,
                        )
                            .filter { it.matchRank != null }
                            .take(itemsToTake)
                    }

                    false -> {
                        emptyList()
                    }
                },
            )

            Flog.persist(tag = "Autofill", message = "FillRequest: Response built, authenticated=${fillRequestSpec.authenticated}")
            fillCallback.onSuccess(response)
        } catch (e: Exception) {
            Flog.tag(AutofillTag).e(e)
            Flog.persist(tag = "Autofill", message = "FillRequest: Error ${e.message}")
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