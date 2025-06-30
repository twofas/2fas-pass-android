/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.data.push.internal.PushFactory
import com.twofasapp.data.push.internal.PushLogger
import com.twofasapp.data.push.notifications.NotificationsHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class PushMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val appBuild: AppBuild by inject()
    private val pushRepository: PushRepository by inject()
    private val notificationsHandler: NotificationsHandler by inject()

    override fun onCreate() {
        super.onCreate()
        PushLogger.log("FCM service created")
        scope.launch {
            pushRepository.observePushesOnNotificationChannel().collect { push ->
                notificationsHandler.handle(push)
            }
        }
    }

    override fun onNewToken(token: String) {
        if (appBuild.debuggable) {
            PushLogger.logToken(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (appBuild.debuggable) {
            PushLogger.logMessage(remoteMessage)
        }

        PushFactory.createPush(remoteMessage)?.let {
            pushRepository.dispatchPush(it)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        PushLogger.log("FCM service destroyed")
        super.onDestroy()
    }
}