/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.lockoutsettings

import com.twofasapp.data.settings.domain.AppLockAttempts
import com.twofasapp.data.settings.domain.AppLockTime
import com.twofasapp.data.settings.domain.AutofillLockTime

internal data class LockoutSettingsUiState(
    val appLockTime: AppLockTime = AppLockTime.Default,
    val appLockAttempts: AppLockAttempts = AppLockAttempts.Default,
    val autofillLockTime: AutofillLockTime = AutofillLockTime.Default,
)