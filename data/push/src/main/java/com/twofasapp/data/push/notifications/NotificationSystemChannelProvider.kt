/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.push.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color

class NotificationSystemChannelProvider(private val notificationManager: NotificationManager) {
    fun getBrowserRequestChannelId(): String {
        val channelId = "Browser Request"
        val channelName = "Browser Request"
        val channelImportance = NotificationManager.IMPORTANCE_HIGH

        val channel = NotificationChannel(channelId, channelName, channelImportance)
        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.enableVibration(true)
        notificationManager.createNotificationChannel(channel)

        return channelId
    }
}