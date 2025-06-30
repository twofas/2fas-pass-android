/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.developer.ui

import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.data.purchases.domain.SubscriptionPlan
import com.twofasapp.data.security.crypto.Seed

internal data class DeveloperUiState(
    val appBuild: AppBuild? = null,
    val seed: Seed? = null,
    val loginItemsCount: Int = 0,
    val subscriptionPlan: SubscriptionPlan = SubscriptionPlan.Free(),
    val overrideSubscriptionPlan: SubscriptionPlan? = null,
)