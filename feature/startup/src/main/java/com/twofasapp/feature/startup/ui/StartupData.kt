/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui

import kotlinx.serialization.Serializable

@Serializable
internal data class StartupData(
    val timestamp: Long,
    val words: List<String>,
    val entropyHex: String,
    val seedHex: String,
    val saltHex: String,
    val masterKeyHashHex: String,
) {
    companion object {
        val Empty = StartupData(
            timestamp = 0L,
            words = emptyList(),
            entropyHex = "",
            seedHex = "",
            saltHex = "",
            masterKeyHashHex = "",
        )
    }
}