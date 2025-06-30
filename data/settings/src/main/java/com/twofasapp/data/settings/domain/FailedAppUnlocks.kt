/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.settings.domain

import java.time.Duration

data class FailedAppUnlocks(
    val lockoutCount: Int,
    val failedAttempts: Int,
    val lastFailedAttemptSinceBoot: Long,
) {
    companion object {
        val Empty = FailedAppUnlocks(
            lockoutCount = 0,
            failedAttempts = 0,
            lastFailedAttemptSinceBoot = 0,
        )

        private val lockoutDurations = listOf(
            0L,
            Duration.ofMinutes(1).toMillis(),
            Duration.ofMinutes(3).toMillis(),
            Duration.ofMinutes(15).toMillis(),
            Duration.ofMinutes(60).toMillis(),
        )
    }

    val lockoutDuration: Long
        get() = lockoutDurations.getOrElse(lockoutCount) { lockoutDurations.last() }
}