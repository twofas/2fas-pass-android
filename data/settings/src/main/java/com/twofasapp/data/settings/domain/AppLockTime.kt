/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.settings.domain

import java.time.Duration

enum class AppLockTime(val millis: Long) {
    Immediately(Duration.ofSeconds(1).toMillis()),
    Seconds30(Duration.ofSeconds(30).toMillis()),
    Minute1(Duration.ofMinutes(1).toMillis()),
    Minute5(Duration.ofMinutes(5).toMillis()),
    Hour1(Duration.ofHours(1).toMillis()),
    ;

    companion object {
        val Default: AppLockTime = Seconds30
    }
}