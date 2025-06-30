/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.service.parser

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class NodeStructure(
    val packageName: String?,
    val webDomain: String?,
    val inputs: List<AutofillInput>,
) : Parcelable {

    companion object {
        val Empty: NodeStructure
            get() = NodeStructure(
                packageName = null,
                webDomain = null,
                inputs = mutableListOf(),
            )
    }

    override fun toString(): String {
        return """
            NodeStructure(
                packageName=$packageName
                webDomain=$webDomain
                inputs=[
                    ${inputs.joinToString("\n                    ") { it.toString() }}
                ]
            )
        """.trimIndent()
    }
}