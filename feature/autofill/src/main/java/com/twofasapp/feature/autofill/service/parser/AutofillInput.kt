/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.service.parser

import android.os.Parcelable
import android.view.autofill.AutofillId
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed interface AutofillInput : Parcelable {
    val id: AutofillId
    val matchConfidence: MatchConfidence

    @IgnoredOnParcel
    val node: AutofillNode?

    data class Username(
        override val id: AutofillId,
        override val matchConfidence: MatchConfidence,
        @IgnoredOnParcel override val node: AutofillNode? = null,
    ) : AutofillInput

    data class Password(
        override val id: AutofillId,
        override val matchConfidence: MatchConfidence,
        @IgnoredOnParcel override val node: AutofillNode? = null,
    ) : AutofillInput
}