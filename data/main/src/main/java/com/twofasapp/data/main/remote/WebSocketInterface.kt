/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.remote

import com.twofasapp.data.main.websocket.messages.IncomingMessageJson
import com.twofasapp.data.main.websocket.messages.OutgoingMessageJson
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.serialization.json.Json
import timber.log.Timber

internal class WebSocketInterface(
    private val session: WebSocketSession,
    private val json: Json,
) {
    companion object {
        private val Tag = "WebSocketInterface"
    }

    init {
        Timber.tag(Tag).v("Socket opened!")
    }

    suspend fun sendMessage(outgoing: OutgoingMessageJson) {
        session.sendTextMessage(
            json.encodeToString(outgoing),
        )
    }

    suspend fun receiveMessage(frame: Frame): IncomingMessageJson? {
        return frame.readTextMessage()?.let { text ->
            try {
                json.decodeFromString<IncomingMessageJson>(text)
            } catch (e: Exception) {
                return@let null
            }
        }
    }

    suspend fun close() {
        session.close()
    }

    private suspend fun WebSocketSession.sendTextMessage(message: String) {
        Timber.tag(Tag).v("Outgoing -> $message")
        send(Frame.Text(message))
    }

    private fun Frame.readTextMessage(): String? {
        return (this as? Frame.Text)?.readText()?.let { msg ->
            Timber.tag(Tag).v("Incoming <- $msg")
            msg
        }
    }
}