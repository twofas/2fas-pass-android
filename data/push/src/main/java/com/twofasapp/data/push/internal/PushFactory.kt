/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.push.internal

import com.google.firebase.messaging.RemoteMessage
import com.twofasapp.data.push.domain.Push
import java.time.Instant

internal object PushFactory {
    fun createPush(remoteMessage: RemoteMessage): Push? {
        return when (remoteMessage.data["messageType"]?.lowercase()) {
            "be_request" -> createBrowserRequest(remoteMessage)
            else -> null
        }
    }

    private fun createBrowserRequest(remoteMessage: RemoteMessage): Push? {
        return try {
            Push.BrowserRequest(
                notificationId = remoteMessage.data["notificationId"] ?: return null,
                timestamp = Instant.ofEpochMilli(remoteMessage.data["timestamp"]!!.toLong()),
                pkPersBe = remoteMessage.data["pkPersBe"]!!,
                pkEpheBe = remoteMessage.data["pkEpheBe"]!!,
                sigPush = remoteMessage.data["sigPush"]!!,
                scheme = remoteMessage.data["scheme"]?.toInt(),
            )
        } catch (e: Exception) {
            PushLogger.log(e.message.orEmpty())
            e.printStackTrace()
            null
        }
    }
}