/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.connect.ui.connectmodal

internal sealed interface ConnectState {
    data object Loading : ConnectState
    data object ConfirmNewExtension : ConnectState
    data object UpgradePlan : ConnectState
    data object Success : ConnectState
    data class Error(
        val title: String,
        val subtitle: String,
        val cta: String,
    ) : ConnectState
}