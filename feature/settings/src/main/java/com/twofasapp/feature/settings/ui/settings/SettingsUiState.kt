/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.settings

import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.purchases.domain.SubscriptionPlan

internal data class SettingsUiState(
    val cloudSyncError: Boolean = false,
    val cloudConfig: CloudConfig? = null,
    val subscriptionPlan: SubscriptionPlan = SubscriptionPlan.Free(),
    val scrollToTransferSection: Boolean = false,
)