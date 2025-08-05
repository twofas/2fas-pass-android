/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.quicksetup.ui

import com.twofasapp.core.common.domain.SecurityType

internal data class QuickSetupUiState(
    val syncEnabled: Boolean = false,
    val securityType: SecurityType = SecurityType.Tier3,
)