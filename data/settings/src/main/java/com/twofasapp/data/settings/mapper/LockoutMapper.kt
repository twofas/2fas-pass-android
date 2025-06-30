/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.settings.mapper

import com.twofasapp.data.settings.domain.AppLockAttempts
import com.twofasapp.data.settings.domain.AppLockTime
import com.twofasapp.data.settings.domain.AutofillLockTime
import com.twofasapp.data.settings.domain.FailedAppUnlocks
import com.twofasapp.data.settings.local.model.AppLockAttemptsEntity
import com.twofasapp.data.settings.local.model.AppLockTimeEntity
import com.twofasapp.data.settings.local.model.AutofillLockTimeEntity
import com.twofasapp.data.settings.local.model.FailedAppUnlocksEntity

internal fun AppLockTimeEntity.asDomain(): AppLockTime {
    return when (this) {
        AppLockTimeEntity.Immediately -> AppLockTime.Immediately
        AppLockTimeEntity.Seconds30 -> AppLockTime.Seconds30
        AppLockTimeEntity.Minute1 -> AppLockTime.Minute1
        AppLockTimeEntity.Minute5 -> AppLockTime.Minute5
        AppLockTimeEntity.Hour1 -> AppLockTime.Hour1
    }
}

internal fun AppLockTime.asEntity(): AppLockTimeEntity {
    return when (this) {
        AppLockTime.Immediately -> AppLockTimeEntity.Immediately
        AppLockTime.Seconds30 -> AppLockTimeEntity.Seconds30
        AppLockTime.Minute1 -> AppLockTimeEntity.Minute1
        AppLockTime.Minute5 -> AppLockTimeEntity.Minute5
        AppLockTime.Hour1 -> AppLockTimeEntity.Hour1
    }
}

internal fun AppLockAttemptsEntity.asDomain(): AppLockAttempts {
    return when (this) {
        AppLockAttemptsEntity.Count3 -> AppLockAttempts.Count3
        AppLockAttemptsEntity.Count5 -> AppLockAttempts.Count5
        AppLockAttemptsEntity.Count10 -> AppLockAttempts.Count10
        AppLockAttemptsEntity.NoLimit -> AppLockAttempts.NoLimit
    }
}

internal fun AppLockAttempts.asEntity(): AppLockAttemptsEntity {
    return when (this) {
        AppLockAttempts.Count3 -> AppLockAttemptsEntity.Count3
        AppLockAttempts.Count5 -> AppLockAttemptsEntity.Count5
        AppLockAttempts.Count10 -> AppLockAttemptsEntity.Count10
        AppLockAttempts.NoLimit -> AppLockAttemptsEntity.NoLimit
    }
}

internal fun AutofillLockTimeEntity.asDomain(): AutofillLockTime {
    return when (this) {
        AutofillLockTimeEntity.Minutes5 -> AutofillLockTime.Minutes5
        AutofillLockTimeEntity.Minutes15 -> AutofillLockTime.Minutes15
        AutofillLockTimeEntity.Minutes30 -> AutofillLockTime.Minutes30
        AutofillLockTimeEntity.Hour1 -> AutofillLockTime.Hour1
        AutofillLockTimeEntity.Day1 -> AutofillLockTime.Day1
        AutofillLockTimeEntity.Never -> AutofillLockTime.Never
    }
}

internal fun AutofillLockTime.asEntity(): AutofillLockTimeEntity {
    return when (this) {
        AutofillLockTime.Minutes5 -> AutofillLockTimeEntity.Minutes5
        AutofillLockTime.Minutes15 -> AutofillLockTimeEntity.Minutes15
        AutofillLockTime.Minutes30 -> AutofillLockTimeEntity.Minutes30
        AutofillLockTime.Hour1 -> AutofillLockTimeEntity.Hour1
        AutofillLockTime.Day1 -> AutofillLockTimeEntity.Day1
        AutofillLockTime.Never -> AutofillLockTimeEntity.Never
    }
}

internal fun FailedAppUnlocks.asEntity(): FailedAppUnlocksEntity {
    return FailedAppUnlocksEntity(
        lockoutCount = lockoutCount,
        failedAttempts = failedAttempts,
        lastFailedAttemptSinceBoot = lastFailedAttemptSinceBoot,
    )
}

internal fun FailedAppUnlocksEntity.asDomain(): FailedAppUnlocks {
    return FailedAppUnlocks(
        lockoutCount = lockoutCount,
        failedAttempts = failedAttempts,
        lastFailedAttemptSinceBoot = lastFailedAttemptSinceBoot,
    )
}