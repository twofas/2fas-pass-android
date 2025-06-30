/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.purchases

import com.twofasapp.data.purchases.domain.SubscriptionPlan
import kotlinx.coroutines.flow.Flow

interface PurchasesRepository {
    fun initialize()
    suspend fun fetchSubscriptionInfo()
    suspend fun restorePurchase()
    fun observeSubscriptionPlan(): Flow<SubscriptionPlan>
    suspend fun getSubscriptionPlan(): SubscriptionPlan
}