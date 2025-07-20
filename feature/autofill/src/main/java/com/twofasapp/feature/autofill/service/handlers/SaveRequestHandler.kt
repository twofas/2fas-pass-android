/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.service.handlers

import android.app.assist.AssistStructure
import android.content.Context
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import android.view.autofill.AutofillId
import com.twofasapp.core.android.ktx.getSafelyParcelableNullable
import com.twofasapp.data.main.LoginsRepository
import com.twofasapp.data.purchases.PurchasesRepository
import com.twofasapp.feature.autofill.service.PassAutofillService.Companion.AutofillTag
import com.twofasapp.feature.autofill.service.builders.IntentBuilders
import com.twofasapp.feature.autofill.service.domain.SaveLoginData
import com.twofasapp.feature.autofill.service.domain.SaveRequestSpec
import com.twofasapp.feature.autofill.service.parser.AutofillInput
import timber.log.Timber

internal class SaveRequestHandler(
    private val loginsRepository: LoginsRepository,
    private val purchasesRepository: PurchasesRepository,
) {
    suspend fun handleRequest(
        context: Context,
        saveRequest: SaveRequest,
        saveCallback: SaveCallback,
    ) {
        val saveRequestSpec = saveRequest.clientState.getSafelyParcelableNullable<SaveRequestSpec>(SaveRequestSpec.BundleKey)
        Timber.tag(AutofillTag).d("\uD83D\uDCBE $saveRequestSpec")

        if (saveRequestSpec == null) {
            saveCallback.onFailure("Save request has no payload")
        }
        if (saveRequestSpec!!.inputs.isEmpty()) {
            saveCallback.onFailure("No inputs found")
            return
        }

        if (saveRequestSpec.packageName == context.packageName) {
            saveCallback.onFailure("Package name is the same as autofill service package name!")
            return
        }

        val sessionContext = saveRequest.fillContexts.firstOrNull { it.requestId == saveRequestSpec.autofillSessionId }

        if (sessionContext == null) {
            if (saveRequestSpec.inputs.isEmpty()) {
                saveCallback.onFailure("Session fill context not found")
                return
            }
        }

        if (loginsRepository.getLoginsCount() >= purchasesRepository.getSubscriptionPlan().entitlements.itemsLimit) {
            saveCallback.onFailure("Logins limit reached")
            return
        }

        try {
            val structure = sessionContext!!.structure
            val windowNodes = (0 until structure.windowNodeCount).map { structure.getWindowNodeAt(it) }

            val username = saveRequestSpec.inputs
                .filterIsInstance<AutofillInput.Username>()
                .minByOrNull { it.matchConfidence.rankValue }
                ?.let { windowNodes.findNodeTextValueById(it.id) }

            val password = saveRequestSpec.inputs
                .filterIsInstance<AutofillInput.Password>()
                .minByOrNull { it.matchConfidence.rankValue }
                ?.let { windowNodes.findNodeTextValueById(it.id) }

            if (username == null && password == null) {
                saveCallback.onFailure("Username and password not found")
                return
            }

            val saveLoginData = SaveLoginData(
                uri = saveRequestSpec.uri,
                username = username,
                password = password,
            )

            Timber.tag(AutofillTag).d("\uD83D\uDCBE $saveLoginData")

            context.startActivity(
                IntentBuilders.createSaveLoginIntent(
                    context = context,
                    saveLoginData = saveLoginData,
                ),
            )

            saveCallback.onSuccess()
        } catch (e: Exception) {
            saveCallback.onFailure(e.message)
        }
    }
}

private fun List<AssistStructure.WindowNode>.findNodeTextValueById(id: AutofillId): String? {
    forEach { windowNode ->
        val child = windowNode.rootViewNode.findNodeChildById(id)

        child?.autofillValue?.textValue?.toString()?.let {
            if (it.isNotBlank()) {
                return it
            }
        }
    }

    return null
}

private fun AssistStructure.ViewNode.findNodeChildById(id: AutofillId): AssistStructure.ViewNode? {
    if (autofillId == id) {
        return this
    }

    for (i in 0 until childCount) {
        val child = getChildAt(i).findNodeChildById(id)

        if (child != null) {
            return child
        }
    }

    return null
}