/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.settings.domain

import java.time.Duration

enum class AutofillLockTime(val millis: Long) {
    Minutes5(Duration.ofMinutes(5).toMillis()),
    Minutes15(Duration.ofMinutes(15).toMillis()),
    Minutes30(Duration.ofMinutes(30).toMillis()),
    Hour1(Duration.ofHours(1).toMillis()),
    Day1(Duration.ofDays(1).toMillis()),
    Never(Long.MAX_VALUE),
    ;

    companion object {
        val Default: AutofillLockTime = Hour1
    }
}