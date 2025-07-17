/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.settings

import com.twofasapp.core.common.domain.PasswordGeneratorSettings
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.SelectedTheme
import com.twofasapp.data.settings.domain.AppLockAttempts
import com.twofasapp.data.settings.domain.AppLockTime
import com.twofasapp.data.settings.domain.AutofillLockTime
import com.twofasapp.data.settings.domain.AutofillSettings
import com.twofasapp.data.settings.domain.LoginClickAction
import com.twofasapp.data.settings.domain.SortingMethod
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSelectedTheme(): Flow<SelectedTheme>
    suspend fun setSelectedTheme(theme: SelectedTheme)
    fun observeDynamicColors(): Flow<Boolean>
    suspend fun setDynamicColors(enabled: Boolean)
    fun observeLoginClickAction(): Flow<LoginClickAction>
    suspend fun setLoginClickAction(action: LoginClickAction)
    fun observeAutofillSettings(): Flow<AutofillSettings>
    suspend fun setAutofillSettings(useInline: Boolean? = null)
    fun observeSortingMethod(): Flow<SortingMethod>
    suspend fun setSortingMethod(sortingMethod: SortingMethod)
    fun observeAppLockTime(): Flow<AppLockTime>
    suspend fun setAppLockTime(time: AppLockTime)
    fun observeAppLockAttempts(): Flow<AppLockAttempts>
    suspend fun setAppLockAttempts(attempts: AppLockAttempts)
    fun observeAutofillLockTime(): Flow<AutofillLockTime>
    suspend fun setAutofillLockTime(time: AutofillLockTime)
    fun observeSendCrashLogs(): Flow<Boolean>
    suspend fun setSendCrashLogs(enabled: Boolean)
    fun observeDefaultSecurityType(): Flow<SecurityType>
    suspend fun setDefaultSecurityType(type: SecurityType)
    fun observeScreenCaptureEnabled(): Flow<Boolean>
    suspend fun setScreenCaptureEnabled(enabled: Boolean)
    fun observePasswordGeneratorSettings(): Flow<PasswordGeneratorSettings>
    suspend fun setPasswordGeneratorSettings(settings: PasswordGeneratorSettings)
}