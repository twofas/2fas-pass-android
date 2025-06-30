/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.security.crypto

import com.twofasapp.core.common.ktx.encodeHex
import com.twofasapp.core.common.ktx.sha256

data class Seed(
    val words: List<String>,
    val entropyHex: String,
    val seedHex: String,
    val saltHex: String = words
        .takeLast(4)
        .joinToString("")
        .toByteArray()
        .sha256()
        .encodeHex()
        .take(32),
)