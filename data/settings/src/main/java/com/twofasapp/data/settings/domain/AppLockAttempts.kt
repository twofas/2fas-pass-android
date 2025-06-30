/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.settings.domain

enum class AppLockAttempts(val maxAttempts: Int?) {
    Count3(3),
    Count5(5),
    Count10(10),
    NoLimit(null),
    ;

    companion object {
        val Default: AppLockAttempts = Count3
    }
}