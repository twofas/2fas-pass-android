/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.websocket

import com.twofasapp.data.main.domain.ConnectData
import com.twofasapp.data.main.domain.ConnectWebSocketResult

interface ConnectWebSocket {
    suspend fun open(connectData: ConnectData): ConnectWebSocketResult
}