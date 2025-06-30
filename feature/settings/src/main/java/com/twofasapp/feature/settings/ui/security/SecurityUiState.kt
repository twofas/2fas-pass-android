/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.security

import com.twofasapp.core.common.domain.LoginSecurityType

internal data class SecurityUiState(
    val biometricsEnabled: Boolean = false,
    val screenCaptureEnabled: Boolean = false,
    val defaultSecurityType: LoginSecurityType = LoginSecurityType.Tier1,
)