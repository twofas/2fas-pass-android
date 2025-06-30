/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.android.compose

import androidx.biometric.BiometricManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

enum class BiometricsState {
    Available,
    NotEnrolled,
    NotAvailable,
}

@Composable
fun biometricsState(): BiometricsState {
    if (LocalInspectionMode.current) {
        return BiometricsState.Available
    }

    val context = LocalContext.current

    return remember {
        when (BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricsState.NotAvailable
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricsState.NotEnrolled
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricsState.NotAvailable
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricsState.NotAvailable
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricsState.NotAvailable
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BiometricsState.NotAvailable
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricsState.Available
            else -> BiometricsState.NotAvailable
        }
    }
}