/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.domain

sealed interface AuthStatus {
    val requireAuth: Boolean

    sealed interface Valid : AuthStatus {
        override val requireAuth: Boolean get() = false

        data object Authenticated : Valid
        data object SessionValid : Valid
    }

    sealed interface Invalid : AuthStatus {
        override val requireAuth: Boolean get() = true

        data object NotAuthenticated : Invalid
        data object SessionExpired : Invalid
        data object AppBackgrounded : Invalid
    }
}