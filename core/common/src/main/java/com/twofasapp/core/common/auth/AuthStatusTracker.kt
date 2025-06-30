/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.auth

import com.twofasapp.core.common.domain.AuthStatus
import kotlinx.coroutines.flow.Flow

interface AuthStatusTracker {
    fun observeAuthStatus(): Flow<AuthStatus>
    fun observeIsAuthenticated(): Flow<Boolean>
    suspend fun isAuthenticated(): Boolean
    suspend fun authenticate()
    suspend fun onAppForeground()
    suspend fun onAppBackground()
}