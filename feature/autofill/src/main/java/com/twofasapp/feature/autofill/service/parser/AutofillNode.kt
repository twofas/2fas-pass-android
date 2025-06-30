/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.service.parser

import android.app.assist.AssistStructure.ViewNode
import android.text.InputType
import android.view.View
import android.view.autofill.AutofillId
import android.widget.EditText

internal data class AutofillNode(
    val id: AutofillId,
    val autofillHints: Set<String>,
    val keywords: Set<String>,
    val siblingsKeywords: Set<String>,
    val parentsKeywords: Set<String>,
    val inputType: Int,
    val htmlAttributes: Set<String>,
) {
    companion object {
        fun from(
            viewNode: ViewNode,
            siblingsKeywords: Set<String>,
            parentsKeywords: Set<String>,
        ): AutofillNode? {
            if (viewNode.autofillId == null) {
                return null
            }

            if (viewNode.isInputField.not()) {
                return null
            }

            if (viewNode.importantForAutofill == View.IMPORTANT_FOR_AUTOFILL_NO) {
                return null
            }

            if (viewNode.importantForAutofill == View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS) {
                return null
            }

            if (viewNode.childCount > 0) {
                return null
            }

            return AutofillNode(
                id = viewNode.autofillId!!,
                autofillHints = viewNode.autofillHints.orEmpty().toSet(),
                keywords = viewNode.keywords,
                siblingsKeywords = siblingsKeywords,
                parentsKeywords = parentsKeywords,
                inputType = viewNode.inputType,
                htmlAttributes = viewNode.htmlInputAttributes,
            )
        }
    }

    fun log(): String {
        return """
            ⭐ Node
                - id=$id,
                - inputType=$inputType,
                - autofillHints=$autofillHints,
                - keywords=$keywords,
                - siblingsKeywords=$siblingsKeywords,
                - parentKeywords=$parentsKeywords,
        """.trimIndent()
    }
}

internal fun String.sanitizeHint(): String? =
    this.lowercase()
        .filterNot { it in "_- " }
        .takeIf { it.isNotBlank() }

internal fun Int.matchesInputType(flags: Set<Int>): Boolean {
    return flags.any { this and InputType.TYPE_MASK_VARIATION == it } &&
        (this and InputType.TYPE_TEXT_FLAG_MULTI_LINE) != InputType.TYPE_TEXT_FLAG_MULTI_LINE
}

internal val ViewNode.keywords: Set<String>
    get() = setOfNotNull(
        idEntry?.sanitizeHint(),
        hint?.sanitizeHint(),
        text?.toString()?.sanitizeHint(),
    )

private val ViewNode.htmlInputAttributes: Set<String>
    get() = when {
        htmlInfo?.tag != "input" -> emptySet()
        else ->
            htmlInfo?.attributes
                ?.filter { it.first == "type" }
                ?.mapNotNull { it.second?.sanitizeHint() }
                ?.toSet()
                .orEmpty()
    }

private val ViewNode.isInputField
    get() = className == EditText::class.java.name || htmlInfo?.tag == "input" || autofillHints.isNullOrEmpty().not()