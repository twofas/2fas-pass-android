/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.websocket

import com.twofasapp.data.main.domain.BrowserRequestAction
import com.twofasapp.data.main.domain.BrowserRequestData
import com.twofasapp.data.main.domain.BrowserRequestResponse
import com.twofasapp.data.main.domain.RequestWebSocketResult

interface RequestWebSocket {
    suspend fun open(
        requestData: BrowserRequestData,
        onBrowserRequestAction: suspend (BrowserRequestAction) -> BrowserRequestResponse,
    ): RequestWebSocketResult
}