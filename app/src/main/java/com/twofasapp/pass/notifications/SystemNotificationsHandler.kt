/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.notifications

import com.twofasapp.data.push.domain.Push
import com.twofasapp.data.push.notifications.NotificationsHandler
import com.twofasapp.pass.notifications.browserrequest.BrowserRequestNotification

internal class SystemNotificationsHandler(
    private val browserRequestNotification: BrowserRequestNotification,
) : NotificationsHandler {
    override suspend fun handle(push: Push.NotificationType) {
        when (push) {
            is Push.BrowserRequest -> browserRequestNotification.notify(push)
        }
    }
}