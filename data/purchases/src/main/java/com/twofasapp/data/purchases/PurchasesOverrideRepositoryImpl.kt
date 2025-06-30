/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.purchases

import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.storage.DataStoreOwner
import com.twofasapp.core.common.storage.stringPrefNullable
import com.twofasapp.data.purchases.domain.SubscriptionPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class PurchasesOverrideRepositoryImpl(
    dataStoreOwner: DataStoreOwner,
    private val dispatchers: Dispatchers,
) : PurchasesOverrideRepository, DataStoreOwner by dataStoreOwner {

    private val overridePlan by stringPrefNullable()

    override fun observeOverrideSubscriptionPlan(): Flow<SubscriptionPlan?> {
        return overridePlan.asFlow().map {
            when (it) {
                "free" -> SubscriptionPlan.PreviewFree
                "paid" -> SubscriptionPlan.PreviewPaid
                else -> null
            }
        }
    }

    override suspend fun setOverrideSubscriptionPlan(plan: String?) {
        withContext(dispatchers.io) {
            overridePlan.set(plan)
        }
    }
}