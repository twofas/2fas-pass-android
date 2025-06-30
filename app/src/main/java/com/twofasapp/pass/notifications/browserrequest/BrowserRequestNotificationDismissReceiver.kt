/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.notifications.browserrequest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.twofasapp.core.android.ktx.runSafely
import com.twofasapp.data.main.BrowserExtensionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BrowserRequestNotificationDismissReceiver : BroadcastReceiver(), KoinComponent {
    private val browserExtensionRepository: BrowserExtensionRepository by inject()

    companion object {
        const val NotificationId = "NotificationId"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        CoroutineScope(Dispatchers.IO).launch {
            runSafely {
                browserExtensionRepository.deleteRequest(notificationId = intent?.getStringExtra(NotificationId) ?: return@launch)
            }
        }
    }
}