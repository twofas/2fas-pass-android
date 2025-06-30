/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.push

import com.twofasapp.data.push.domain.Push
import com.twofasapp.data.push.internal.PushLogger
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

internal class PushRepositoryImpl : PushRepository {

    private val notificationChannel: MutableSharedFlow<Push.NotificationType> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val localChannel: MutableSharedFlow<Push.LocalType> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override fun observePushesOnNotificationChannel(): Flow<Push.NotificationType> {
        return notificationChannel
    }

    override fun observePushesOnLocalChannel(): Flow<Push.LocalType> {
        return localChannel
    }

    override fun dispatchPush(push: Push) {
        when {
            push is Push.NotificationType && push is Push.LocalType -> {
                val dispatchSuccess = dispatchLocalPush(push)
                if (dispatchSuccess.not()) {
                    dispatchNotificationPush(push)
                }
            }

            push is Push.NotificationType -> dispatchNotificationPush(push)
            push is Push.LocalType -> dispatchLocalPush(push)
        }
    }

    private fun dispatchNotificationPush(push: Push.NotificationType): Boolean {
        PushLogger.log("\uD83D\uDD14 Dispatch Notification => $push")
        notificationChannel.tryEmit(push)
        return true
    }

    private fun dispatchLocalPush(push: Push.LocalType): Boolean {
        return if (localChannel.subscriptionCount.value == 0) {
            false
        } else {
            PushLogger.log("\uD83D\uDD14 Dispatch Local => $push")
            localChannel.tryEmit(push)
            true
        }
    }
}