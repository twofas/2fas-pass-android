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
internal sealed class MatchConfidence(
    val rankValue: Int,
) : Parcelable {
    data class Exact(private val rank: Int) : MatchConfidence(rank)
    data class Strong(private val rank: Int) : MatchConfidence(rank)
    data class Weak(private val rank: Int) : MatchConfidence(rank)
}