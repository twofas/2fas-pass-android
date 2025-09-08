/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.settings

import com.twofasapp.data.settings.domain.FailedAppUnlocks
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    suspend fun getAppVersionCode(): Long
    suspend fun setAppVersionCode(versionCode: Long)
    fun observeStartupCompleted(): Flow<Boolean>
    suspend fun setStartupCompleted(completed: Boolean)
    fun observeBiometricsPrompted(): Flow<Boolean>
    suspend fun setBiometricsPrompted(prompted: Boolean)
    fun observeConnectOnboardingPrompted(): Flow<Boolean>
    suspend fun setConnectOnboardingPrompted(prompted: Boolean)
    fun observeFailedAppUnlocks(): Flow<FailedAppUnlocks?>
    suspend fun setFailedAppUnlocks(failedAppUnlocks: FailedAppUnlocks?)
    fun observeQuickSetupPrompted(): Flow<Boolean>
    suspend fun setQuickSetupPrompted(prompted: Boolean)
    suspend fun getAppUpdatePrompted(): Boolean
    suspend fun setAppUpdatePrompted(prompted: Boolean)
}