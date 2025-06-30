/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.subscription

import com.twofasapp.data.purchases.domain.SubscriptionPlan

internal data class ManageSubscriptionUiState(
    val subscriptionPlan: SubscriptionPlan.Paid? = null,
    val itemsCount: Int = 0,
    val browsersCount: Int = 0,
)