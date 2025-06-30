/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.purchases.domain

import java.time.Duration
import java.time.Instant

sealed interface SubscriptionPlan {
    val entitlements: Entitlements
    val displayName: String

    data class Free(
        override val entitlements: Entitlements = Entitlements.Free,
        override val displayName: String = "Free Plan",
    ) : SubscriptionPlan

    data class Paid(
        override val entitlements: Entitlements = Entitlements.Paid,
        override val displayName: String,
        val active: Boolean,
        val userId: String,
        val priceFormatted: String,
        val entitlementId: String,
        val productId: String,
        val productPlanId: String?,
        val originalPurchaseDate: Instant,
        val latestPurchaseDate: Instant,
        val expirationDate: Instant?,
        val unsubscribeDate: Instant?,
        val store: String,
        val willRenew: Boolean,
        val sandbox: Boolean,
        val rawData: String,
    ) : SubscriptionPlan

    companion object {
        val PreviewFree = Free()
        val PreviewPaid = Paid(
            displayName = "Unlimited (Test)",
            active = true,
            userId = "\$RCAnonymousID:test123",
            priceFormatted = "$9.99",
            entitlementId = "test",
            productId = "test",
            productPlanId = "test",
            originalPurchaseDate = Instant.now(),
            latestPurchaseDate = Instant.now(),
            expirationDate = Instant.now().plusMillis(Duration.ofDays(7).toMillis()),
            unsubscribeDate = null,
            store = "test",
            willRenew = true,
            sandbox = true,
            rawData = "{}",
        )
    }
}