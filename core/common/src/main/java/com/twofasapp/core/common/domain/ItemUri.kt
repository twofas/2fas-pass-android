/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.domain

data class ItemUri(
    val text: String,
    val matcher: UriMatcher = UriMatcher.Domain,
) {
    companion object {
        val hostRegex = Regex("""(?:https?://)?(?:www\.)?([^/]+\.[^/]+)""")
    }

    val host: String?
        get() = hostRegex.find(text.trim())?.groupValues?.get(1)

    val iconUrl: String?
        get() = if (host.isNullOrEmpty()) {
            null
        } else {
            "https://icon.2fas.com/$host/favicon.png"
        }
}