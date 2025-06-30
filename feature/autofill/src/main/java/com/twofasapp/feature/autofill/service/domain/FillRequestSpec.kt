/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.service.domain

import android.os.Parcelable
import android.widget.inline.InlinePresentationSpec
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class FillRequestSpec(
    val autofillSessionId: Int,
    val authenticated: Boolean,
    val maxItemsCount: Int,
    val inlinePresentationEnabled: Boolean,
    val inlinePresentationSpecs: List<InlinePresentationSpec>,
    val flags: Int,
) : Parcelable {

    fun getInlinePresentationSpec(index: Int): InlinePresentationSpec? {
        return inlinePresentationSpecs.getOrNull(index) ?: inlinePresentationSpecs.lastOrNull()
    }
}