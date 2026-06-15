/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.push.internal

import com.google.firebase.messaging.RemoteMessage
import com.twofasapp.core.common.logger.Flog
import com.twofasapp.data.push.domain.Push
import java.time.Instant

internal object PushFactory {
    fun createPush(remoteMessage: RemoteMessage): Push? {
        val messageType = remoteMessage.data["messageType"]?.lowercase()
        return when (messageType) {
            "be_request" -> createBrowserRequest(remoteMessage)
            else -> {
                Flog.persist("Push", "Unsupported messageType=$messageType")
                null
            }
        }
    }

    private fun createBrowserRequest(remoteMessage: RemoteMessage): Push? {
        return try {
            val push = Push.BrowserRequest(
                notificationId = remoteMessage.data["notificationId"] ?: return null,
                timestamp = Instant.ofEpochMilli(remoteMessage.data["timestamp"]!!.toLong()),
                pkPersBe = remoteMessage.data["pkPersBe"]!!,
                pkEpheBe = remoteMessage.data["pkEpheBe"]!!,
                sigPush = remoteMessage.data["sigPush"]!!,
                scheme = remoteMessage.data["scheme"]?.toInt(),
            )
            Flog.persist("Push", "Parsed BrowserRequest (scheme=${push.scheme})")
            push
        } catch (e: Exception) {
            Flog.persist("Push", "Failed to parse BrowserRequest: ${e.message}")
            Flog.persist("Push", e)
            PushLogger.log(e.message.orEmpty())
            e.printStackTrace()
            null
        }
    }
}