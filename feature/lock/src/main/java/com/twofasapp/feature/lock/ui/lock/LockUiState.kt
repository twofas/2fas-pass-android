/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.lock.ui.lock

import com.twofasapp.core.common.domain.SelectedTheme
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.data.settings.domain.AppLockAttempts
import com.twofasapp.data.settings.domain.FailedAppUnlocks

internal data class LockUiState(
    val selectedTheme: SelectedTheme = SelectedTheme.Auto,
    val dynamicColors: Boolean = false,
    val appLockAttempts: AppLockAttempts = AppLockAttempts.Default,
    val loading: Boolean = false,
    val biometricsEnabled: Boolean = false,
    val biometricsPrompted: Boolean = false,
    val masterKeyEncryptedWithBiometrics: EncryptedBytes? = null,
    val passwordError: String? = null,
    val appUpdateError: Throwable? = null,
    val failedAppUnlocks: FailedAppUnlocks? = null,
    val locked: Boolean = true,
)