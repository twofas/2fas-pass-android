/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.push.notifications

import com.twofasapp.data.push.domain.Push

interface NotificationsHandler {
    suspend fun handle(push: Push.NotificationType)
}