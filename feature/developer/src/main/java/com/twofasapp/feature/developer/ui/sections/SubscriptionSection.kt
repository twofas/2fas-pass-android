/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.developer.ui.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.ktx.formatDateTime
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonHeight
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.data.purchases.domain.SubscriptionPlan
import com.twofasapp.feature.developer.ui.DeveloperUiState

@Composable
internal fun SubscriptionSection(
    uiState: DeveloperUiState,
    onSetSubscriptionOverride: (String?) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Subscription Plan",
            style = MdtTheme.typo.labelLargeProminent,
            color = MdtTheme.color.primary,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Column(
            modifier = Modifier.selectableGroup(),
        ) {
            SubscriptionEntry(
                title = "Free",
                icon = MdtIcons.Star,
                selected = uiState.subscriptionPlan is SubscriptionPlan.Free,
                highlight = uiState.overrideSubscriptionPlan is SubscriptionPlan.Free,
                onClick = { onSetSubscriptionOverride("free") },
            )

            SubscriptionEntry(
                title = "Paid",
                icon = MdtIcons.StarShine,
                selected = uiState.subscriptionPlan is SubscriptionPlan.Paid,
                highlight = uiState.overrideSubscriptionPlan is SubscriptionPlan.Paid,
                onClick = { onSetSubscriptionOverride("paid") },
            )
        }

        Button(
            text = "Reset Override",
            enabled = uiState.overrideSubscriptionPlan != null,
            size = ButtonHeight.Small,
            onClick = { onSetSubscriptionOverride(null) },
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
        )

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ItemColumn(
                label = "Name",
                value = uiState.subscriptionPlan.displayName,
            )

            ItemColumn(
                label = "Entitlements",
                value = buildString {
                    appendLine("itemsLimit = ${uiState.subscriptionPlan.entitlements.itemsLimit}")
                    appendLine("multiDeviceSync = ${uiState.subscriptionPlan.entitlements.multiDeviceSync}")
                    append("unlimitedConnectedBrowsers = ${uiState.subscriptionPlan.entitlements.unlimitedConnectedBrowsers}")
                },
            )

            (uiState.subscriptionPlan as? SubscriptionPlan.Paid)?.let {
                ItemColumn(label = "Active", value = it.active.toString())
                ItemColumn(label = "UserId", value = it.userId)
                ItemColumn(label = "Price", value = it.priceFormatted)
                ItemColumn(label = "EntitlementId", value = it.entitlementId)
                ItemColumn(label = "ProductId", value = it.productId)
                ItemColumn(label = "ProductPlanId", value = it.productPlanId.toString())
                ItemColumn(label = "OriginalPurchaseDate", value = it.originalPurchaseDate.formatDateTime())
                ItemColumn(label = "LatestPurchaseDate", value = it.latestPurchaseDate.formatDateTime())
                ItemColumn(label = "ExpirationDate", value = it.expirationDate?.formatDateTime().toString())
                ItemColumn(label = "UnsubscribeDate", value = it.unsubscribeDate?.formatDateTime().toString())
                ItemColumn(label = "Store", value = it.store)
                ItemColumn(label = "WillRenew", value = it.willRenew.toString())
                ItemColumn(label = "Sandbox", value = it.sandbox.toString())
                ItemColumn(label = "RawData", value = it.rawData)
            }
        }
    }
}

@Composable
private fun SubscriptionEntry(
    title: String,
    icon: Painter,
    selected: Boolean,
    highlight: Boolean,
    onClick: () -> Unit = {},
) {
    OptionEntry(
        title = title,
        icon = icon,
        titleColor = if (highlight) MdtTheme.color.tertiary else MdtTheme.color.onSurface,
        iconTint = if (highlight) MdtTheme.color.tertiary else MdtTheme.color.onSurface,
        onClick = if (selected) null else onClick,
        content = {
            RadioButton(
                selected = selected,
                onClick = null,
            )
        },
    )
}

@Preview
@Composable
private fun SubscriptionSectionPreview() {
    PreviewTheme {
        SubscriptionSection(DeveloperUiState())
    }
}