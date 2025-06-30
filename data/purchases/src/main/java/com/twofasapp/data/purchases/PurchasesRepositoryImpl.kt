/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.purchases

import android.content.Context
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.revenuecat.purchases.restorePurchasesWith
import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.core.common.build.BuildVariant
import com.twofasapp.core.common.build.LocalConfig
import com.twofasapp.data.purchases.domain.SubscriptionPlan
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class PurchasesRepositoryImpl(
    private val context: Context,
    private val appBuild: AppBuild,
    private val localConfig: LocalConfig,
    private val purchasesOverrideRepository: PurchasesOverrideRepository,
) : PurchasesRepository {

    private val subscriptionPlanFlow = MutableStateFlow<SubscriptionPlan>(SubscriptionPlan.Free())

    @OptIn(DelicateCoroutinesApi::class)
    override fun initialize() {
        if (appBuild.debuggable) {
            Purchases.logLevel = LogLevel.DEBUG
        }

        Purchases.configure(
            configuration = PurchasesConfiguration.Builder(
                context = context,
                apiKey = localConfig.revenueCatPublicKey,
            )
                .build(),
        )

        GlobalScope.launch {
            observeCustomerInfo().collect { customerInfo ->
                subscriptionPlanFlow.update {
                    customerInfo.mapToSubscriptionPlan()
                }
            }
        }
    }

    override suspend fun fetchSubscriptionInfo() {
        Purchases.sharedInstance.getCustomerInfoWith { info ->
            subscriptionPlanFlow.update { info.mapToSubscriptionPlan() }
        }
    }

    override suspend fun restorePurchase() {
        Purchases.sharedInstance.restorePurchasesWith { customerInfo ->
            subscriptionPlanFlow.update { customerInfo.mapToSubscriptionPlan() }
        }
    }

    override fun observeSubscriptionPlan(): Flow<SubscriptionPlan> {
        return when (appBuild.buildVariant) {
            BuildVariant.Release -> subscriptionPlanFlow
            BuildVariant.Internal,
            BuildVariant.Debug,
            -> {
                combine(
                    purchasesOverrideRepository.observeOverrideSubscriptionPlan(),
                    subscriptionPlanFlow,
                ) { overridePlan, subscriptionPlan ->
                    overridePlan ?: subscriptionPlan
                }
            }
        }
    }

    override suspend fun getSubscriptionPlan(): SubscriptionPlan {
        return when (appBuild.buildVariant) {
            BuildVariant.Release -> subscriptionPlanFlow.value
            BuildVariant.Internal,
            BuildVariant.Debug,
            -> purchasesOverrideRepository.observeOverrideSubscriptionPlan().first() ?: subscriptionPlanFlow.value
        }
    }

    private fun CustomerInfo.mapToSubscriptionPlan(): SubscriptionPlan {
        return when (entitlements.active.isEmpty()) {
            true -> {
                SubscriptionPlan.Free()
            }

            false -> {
                with(entitlements.active.values.first()) {
                    SubscriptionPlan.Paid(
                        displayName = "Unlimited",
                        active = isActive,
                        userId = originalAppUserId,
                        priceFormatted = subscriptionsByProductIdentifier[productIdentifier]?.price?.formatted.orEmpty(),
                        entitlementId = identifier,
                        productId = productIdentifier,
                        productPlanId = productPlanIdentifier,
                        originalPurchaseDate = originalPurchaseDate.toInstant(),
                        latestPurchaseDate = latestPurchaseDate.toInstant(),
                        expirationDate = expirationDate?.toInstant(),
                        unsubscribeDate = unsubscribeDetectedAt?.toInstant(),
                        store = store.name,
                        willRenew = willRenew,
                        sandbox = isSandbox,
                        rawData = rawData.toString(),
                    )
                }
            }
        }
    }

    private fun observeCustomerInfo(): Flow<CustomerInfo> = callbackFlow {
        Purchases.sharedInstance.updatedCustomerInfoListener = UpdatedCustomerInfoListener { customerInfo ->
            trySend(customerInfo).isSuccess
        }

        Purchases.sharedInstance.getCustomerInfoWith { info ->
            trySend(info)
        }

        awaitClose {
            Purchases.sharedInstance.removeUpdatedCustomerInfoListener()
        }
    }
}