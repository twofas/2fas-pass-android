/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.service.builders

import android.content.Context
import android.os.Bundle
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.SaveInfo
import com.twofasapp.feature.autofill.service.domain.AutofillLogin
import com.twofasapp.feature.autofill.service.domain.FillRequestSpec
import com.twofasapp.feature.autofill.service.domain.SaveRequestSpec
import com.twofasapp.feature.autofill.service.parser.NodeStructure

internal object FillResponseBuilder {

    fun create(
        context: Context,
        fillRequestSpec: FillRequestSpec,
        nodeStructure: NodeStructure,
        logins: List<AutofillLogin>,
    ): FillResponse {
        val fillResponse = FillResponse.Builder()

        // Add login items
        logins.forEachIndexed { index, login ->
            fillResponse.addDataset(
                DatasetBuilder.createLoginItem(
                    context = context,
                    nodeStructure = nodeStructure,
                    inlinePresentationSpec = fillRequestSpec.getInlinePresentationSpec(index),
                    login = login,
                ),
            )
        }

        // Add app item
        fillResponse.addDataset(
            DatasetBuilder.createAppItem(
                context = context,
                nodeStructure = nodeStructure,
                inlinePresentationSpec = fillRequestSpec.getInlinePresentationSpec(
                    if (fillRequestSpec.inlinePresentationEnabled && logins.isNotEmpty()) {
                        fillRequestSpec.maxItemsCount - 2
                    } else {
                        fillRequestSpec.maxItemsCount - 1
                    },
                ),
            ),
        )

        // Add pinned icon
        if (fillRequestSpec.inlinePresentationEnabled && logins.isNotEmpty()) {
            fillResponse.addDataset(
                DatasetBuilder.createPinnedItem(
                    context = context,
                    nodeStructure = nodeStructure,
                    inlinePresentationSpec = fillRequestSpec.getInlinePresentationSpec(fillRequestSpec.maxItemsCount - 1),
                ),
            )
        }

        val isCompatibilityModeActive = (fillRequestSpec.flags or FillRequest.FLAG_COMPATIBILITY_MODE_REQUEST) == fillRequestSpec.flags

        if (isCompatibilityModeActive.not()) {
            fillResponse
                .setSaveInfo(
                    SaveInfo.Builder(
                        SaveInfo.SAVE_DATA_TYPE_USERNAME or SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                        nodeStructure.inputs.map { it.id }.toTypedArray(),
                    )
                        .setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE)
                        .build(),
                )
                .setClientState(
                    Bundle()
                        .apply {
                            classLoader = SaveRequestSpec::class.java.classLoader

                            putParcelable(
                                SaveRequestSpec.BundleKey,
                                SaveRequestSpec(
                                    autofillSessionId = fillRequestSpec.autofillSessionId,
                                    webDomain = nodeStructure.webDomain,
                                    packageName = nodeStructure.packageName,
                                    inputs = nodeStructure.inputs,
                                ),
                            )
                        },
                )
        }

        return fillResponse.build()
    }
}