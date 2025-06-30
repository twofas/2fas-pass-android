/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.push

import com.twofasapp.data.push.domain.Push
import kotlinx.coroutines.flow.Flow

interface PushRepository {
    fun observePushesOnNotificationChannel(): Flow<Push.NotificationType>
    fun observePushesOnLocalChannel(): Flow<Push.LocalType>
    fun dispatchPush(push: Push)
}