/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.push.domain

import java.time.Instant

/**
 * Interface for all push types.
 * - NotificationType - delivered as system notifications
 * - LocalType - handled locally within the app
 *
 *  If the app is in the background and a push implements both NotificationType and LocalType,
 *  it will be delivered only on the notification channel.
 *
 *  If the app is in the foreground and a push implements both NotificationType and LocalType,
 *  it will be delivered only on the local channel
 */
sealed interface Push {

    sealed interface NotificationType : Push
    sealed interface LocalType : Push

    data class BrowserRequest(
        val notificationId: String,
        val timestamp: Instant,
        val pkPersBe: String,
        val pkEpheBe: String,
        val sigPush: String,
        val scheme: Int?,
    ) : NotificationType, LocalType
}