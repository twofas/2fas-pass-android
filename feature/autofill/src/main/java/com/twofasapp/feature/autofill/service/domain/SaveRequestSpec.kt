/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.service.domain

import android.os.Parcelable
import com.twofasapp.feature.autofill.service.parser.AutofillInput
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class SaveRequestSpec(
    val autofillSessionId: Int,
    val packageName: String?,
    val webDomain: String?,
    val inputs: List<AutofillInput>,
) : Parcelable {
    companion object {
        const val BundleKey: String = "SaveRequestSpec"
    }

    val uri: String?
        get() = webDomain ?: packageName
}