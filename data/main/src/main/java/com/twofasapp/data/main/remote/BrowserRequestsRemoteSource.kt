/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.remote

import com.twofasapp.data.main.remote.model.NotificationsJson
import com.twofasapp.data.main.websocket.messages.IncomingMessageJson
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.wss
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.json.Json

internal class BrowserRequestsRemoteSource(
    private val httpClient: HttpClient,
    private val json: Json,
) {
    suspend fun openWebSocket(
        sessionIdHex: String,
        onOpened: suspend WebSocketInterface.() -> Unit,
        onMessageReceived: suspend WebSocketInterface.(IncomingMessageJson?) -> Unit = {},
    ) {
        httpClient.wss(
            urlString = "wss://pass.2fas.com/proxy/mobile/$sessionIdHex",
            request = { header("Sec-WebSocket-Protocol", "2FAS-Pass") },
        ) {
            val ws = WebSocketInterface(
                session = this,
                json = json,
            )

            onOpened(ws)
            incoming.receiveAsFlow().collect { incomingFrame ->
                onMessageReceived(ws, ws.receiveMessage(incomingFrame))
            }
        }
    }

    suspend fun fetchNotifications(deviceId: String): NotificationsJson {
        return httpClient.get("https://pass.2fas.com/device/$deviceId/notifications").body()
    }

    suspend fun deleteNotification(deviceId: String, notificationId: String) {
        httpClient.delete("https://pass.2fas.com/device/$deviceId/notifications/$notificationId")
    }
}