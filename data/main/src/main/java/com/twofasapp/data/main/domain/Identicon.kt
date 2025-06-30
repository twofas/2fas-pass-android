/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.domain

data class Identicon(
    val svgLight: String,
    val svgDark: String,
) {
    companion object {
        val Empty = Identicon(
            svgLight = "",
            svgDark = "",
        )
    }
}