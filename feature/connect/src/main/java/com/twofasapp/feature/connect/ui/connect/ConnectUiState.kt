/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.connect.ui.connect

internal data class ConnectUiState(
    val debuggable: Boolean = false,
    val connectOnboardingPrompted: Boolean? = null,
    val scannerEnabled: Boolean = true,
)