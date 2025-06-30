/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.purchases

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialog
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialogOptions
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.foundation.dialog.InfoDialog
import com.twofasapp.core.locale.MdtLocale

@Composable
fun PurchasesDialog(
    title: String,
    body: String,
    onDismissRequest: () -> Unit,
    onSuccess: () -> Unit = {},
) {
    var showPaywall by remember { mutableStateOf(false) }

    if (showPaywall) {
        PurchasesDialog(
            onDismissRequest = onDismissRequest,
            onSuccess = onSuccess,
        )
    } else {
        InfoDialog(
            onDismissRequest = {
                if (showPaywall.not()) {
                    onDismissRequest()
                }
            },
            title = title,
            body = body,
            icon = MdtIcons.Star,
            negative = MdtLocale.strings.commonCancel,
            positive = MdtLocale.strings.paywallNoticeCta,
            onNegative = onDismissRequest,
            onPositive = { showPaywall = true },
        )
    }
}

@Composable
fun PurchasesDialog(
    onDismissRequest: () -> Unit,
    onSuccess: () -> Unit = {},
) {
    PaywallDialog(
        paywallDialogOptions = PaywallDialogOptions.Builder()
            .setListener(
                object : PaywallListener {
                    override fun onPurchaseCompleted(customerInfo: CustomerInfo, storeTransaction: StoreTransaction) {
                        onDismissRequest()
                        onSuccess()
                    }

                    override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                        onDismissRequest()
                        onSuccess()
                    }
                },
            )
            .setDismissRequest(onDismissRequest)
            .build(),
    )
}