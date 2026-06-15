/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.network

interface ApiConfig {

    companion object {
        const val ProductionApiUrl = "https://pass.2fas.com"
        const val DevApiUrl = "https://dev-pass.2fas.com"

        const val ProductionWssUrl = "wss://pass.2fas.com"
        const val DevWssUrl = "wss://dev-pass.2fas.com"

        const val ProductionShareApiUrl = "https://share.2fas.com"
        const val DevShareApiUrl = "https://dev-share.2fas.com"
    }

    val apiUrl: String
    val wssUrl: String
    val shareApiUrl: String
}