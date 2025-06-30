/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.notifications.browserrequest

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.twofasapp.core.common.build.Device
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.core.locale.Strings
import com.twofasapp.data.main.BrowserExtensionRepository
import com.twofasapp.data.main.ConnectedBrowsersRepository
import com.twofasapp.data.main.domain.BrowserRequestData
import com.twofasapp.data.push.domain.Push
import com.twofasapp.data.push.internal.PushLogger
import com.twofasapp.data.push.notifications.NotificationSystemChannelProvider
import com.twofasapp.pass.notifications.SystemNotification
import com.twofasapp.pass.ui.app.AppActivity
import java.time.Duration

internal class BrowserRequestNotification(
    private val context: Context,
    private val strings: Strings,
    private val device: Device,
    private val notificationManager: NotificationManager,
    private val notificationChannelProvider: NotificationSystemChannelProvider,
    private val connectedBrowsersRepository: ConnectedBrowsersRepository,
    private val browserExtensionRepository: BrowserExtensionRepository,
) : SystemNotification<Push.BrowserRequest> {

    override suspend fun notify(push: Push.BrowserRequest) {
        val browser = connectedBrowsersRepository.getBrowser(push.pkPersBe.decodeBase64())

        if (browser == null) {
            PushLogger.log("Connected browser not found -> push discarded")
            return
        }

        val browserRequestData = BrowserRequestData(
            browser = browser,
            deviceId = device.uniqueId(),
            notificationId = push.notificationId,
            timestamp = push.timestamp.toEpochMilli(),
            pkPersBe = push.pkPersBe.decodeBase64(),
            pkEpheBe = push.pkEpheBe.decodeBase64(),
            signature = push.sigPush.decodeBase64(),
        )

        if (browserExtensionRepository.checkIsRequestValid(browserRequestData).not()) {
            PushLogger.log("Invalid browser request -> push discarded")
            return
        }

        val systemNotificationId = push.timestamp.epochSecond.toInt()
        val contentIntent = createContentIntent(systemNotificationId = systemNotificationId)
        val deleteIntent = createDeleteIntent(systemNotificationId = systemNotificationId, notificationId = push.notificationId)
        val groupKey = "be_request"

        val notification = NotificationCompat.Builder(context, notificationChannelProvider.getBrowserRequestChannelId())
            .setContentTitle(strings.pushBrowserRequestTitle)
            .setContentText(strings.pushBrowserRequestMessage.format(browser.extensionName))
            .setStyle(NotificationCompat.BigTextStyle())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setSmallIcon(com.twofasapp.core.design.R.drawable.push_icon)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setGroup(groupKey)
            .setTimeoutAfter(Duration.ofMinutes(2).toMillis())
            .setContentIntent(contentIntent)
            .setDeleteIntent(deleteIntent)
            .build()

        val summaryNotification = NotificationCompat.Builder(context, notificationChannelProvider.getBrowserRequestChannelId())
            .setSmallIcon(com.twofasapp.core.design.R.drawable.push_icon)
            .setContentTitle("2FAS Pass Requests")
            .setContentText("Click to show more")
            .setStyle(
                NotificationCompat.InboxStyle()
                    .setSummaryText("Open Requests"),
            )
            .setAutoCancel(true)
            .setGroup(groupKey)
            .setGroupSummary(true)
            .setContentIntent(contentIntent)
            .build()

        notificationManager.notify(systemNotificationId, notification)
        notificationManager.notify(216633, summaryNotification)
    }

    private fun createContentIntent(
        systemNotificationId: Int,
    ): PendingIntent {
        val contentIntent = Intent(context, AppActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        return PendingIntent.getActivity(
            /* context = */
            context,
            /* requestCode = */
            systemNotificationId,
            /* intent = */
            contentIntent,
            /* flags = */
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createDeleteIntent(
        systemNotificationId: Int,
        notificationId: String,
    ): PendingIntent {
        val contentIntent = Intent(context, BrowserRequestNotificationDismissReceiver::class.java).apply {
            putExtra(BrowserRequestNotificationDismissReceiver.NotificationId, notificationId)
        }

        return PendingIntent.getBroadcast(
            /* context = */
            context,
            /* requestCode = */
            systemNotificationId,
            /* intent = */
            contentIntent,
            /* flags = */
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}