/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.push.internal

import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

object PushLogger {
    private const val Tag = "PushMessagingService"

    fun logMessage(remoteMessage: RemoteMessage) {
        try {
            Timber.tag(Tag)
                .i("\uD83D\uDD14 Push Received <= data=${remoteMessage.data}, title=${remoteMessage.notification?.title}, body=${remoteMessage.notification?.body}")
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun logToken(token: String) {
        Timber.tag(Tag).i("FcmToken: $token")
    }

    fun log(message: String) {
        Timber.tag(Tag).i(message)
    }
}