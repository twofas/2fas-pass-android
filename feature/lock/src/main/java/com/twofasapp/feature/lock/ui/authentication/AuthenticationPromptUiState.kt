/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.lock.ui.authentication

import com.twofasapp.core.common.domain.crypto.EncryptedBytes

internal data class AuthenticationPromptUiState(
    val initialising: Boolean = true,
    val loading: Boolean = false,
    val biometricsEnabled: Boolean = false,
    val masterKeyEncryptedWithBiometrics: EncryptedBytes? = null,
    val passwordError: String? = null,
)