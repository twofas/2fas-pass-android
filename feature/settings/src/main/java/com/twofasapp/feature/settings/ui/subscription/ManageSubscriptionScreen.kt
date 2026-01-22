/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.subscription

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.copyToClipboard
import com.twofasapp.core.common.ktx.formatDate
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionHeader
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.layout.ActionsRow
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.pullrefresh.PullToRefresh
import com.twofasapp.core.design.foundation.screen.LazyContent
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.state.ScreenState
import com.twofasapp.core.design.theme.RoundedShapeIndexed
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.purchases.domain.SubscriptionPlan
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun ManageSubscriptionScreen(
    viewModel: ManageSubscriptionViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        screenState = screenState,
        onPullRefresh = viewModel::pullRefresh,
    )
}

@Composable
private fun Content(
    uiState: ManageSubscriptionUiState,
    screenState: ScreenState,
    onPullRefresh: () -> Unit = {},
) {
    val strings = MdtLocale.strings
    val context = LocalContext.current

    PullToRefresh(
        onPullRefresh = onPullRefresh,
    ) {
        Scaffold(
            topBar = { TopAppBar(containerColor = MdtTheme.color.transparent) },
        ) { padding ->
            LazyContent(
                screenState = screenState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MdtTheme.color.background),
                contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp),
                itemsWhenSuccess = {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Image(
                                painter = painterResource(com.twofasapp.core.design.R.drawable.brand_logo),
                                contentDescription = null,
                                modifier = Modifier.size(50.dp),
                            )

                            Space(16.dp)

                            Text(
                                text = strings.manageSubscriptionTitle,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                style = MdtTheme.typo.headlineSmall,
                            )

                            Space(8.dp)

                            Text(
                                text = strings.manageSubscriptionUnlimitedDescription,
                                style = MdtTheme.typo.bodyMedium,
                                color = MdtTheme.color.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = ScreenPadding),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    item {
                        OptionHeader(
                            strings.manageSubscriptionTitle,
                        )
                    }

                    item {
                        Entry(
                            title = strings.manageSubscriptionUserIdentifierTitle,
                            subtitle = uiState.subscriptionPlan?.userId,
                            isFirst = true,
                            actions = {
                                ActionsRow(useHorizontalPadding = true) {
                                    IconButton(
                                        icon = MdtIcons.Copy,
                                        onClick = { context.copyToClipboard(uiState.subscriptionPlan?.userId.orEmpty()) },
                                    )
                                }
                            },
                        )
                    }

                    item {
                        Entry(
                            title = strings.manageSubscriptionPlanTitle,
                            subtitle = buildString {
                                appendLine(strings.manageSubscriptionPlanNamePrefix + " " + uiState.subscriptionPlan?.entitlementId)
                                appendLine(strings.manageSubscriptionPlanPricePrefix + " " + uiState.subscriptionPlan?.priceFormatted)
                                appendLine("${if (uiState.subscriptionPlan?.willRenew == true) strings.manageSubscriptionRenewsAt else strings.manageSubscriptionEndsAt} ${uiState.subscriptionPlan?.expirationDate?.formatDate()}")
                            },
                            isLast = true,
                        )
                    }

                    item {
                        OptionHeader(
                            strings.manageSubscriptionBenefitsHeader,
                        )
                    }
                    item {
                        Entry(
                            title = strings.manageSubscriptionItemsInVaultTitle,
                            subtitle = strings.manageSubscriptionItemsInVaultSubtitle.format(uiState.itemsCount),
                            isFirst = true,
                        )
                    }

                    item {
                        Entry(
                            title = strings.manageSubscriptionTrustedExtensionsTitle,
                            subtitle = strings.manageSubscriptionTrustedExtensionsSubtitle.format(uiState.browsersCount),
                        )
                    }

                    item {
                        Entry(
                            title = strings.manageSubscriptionMultiDeviceSyncTitle,
                            subtitle = strings.manageSubscriptionUnlimited,
                            isLast = true,
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun Entry(
    title: String,
    subtitle: String? = null,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(
        modifier = Modifier
            .padding(horizontal = ScreenPadding)
            .fillMaxWidth()
            .background(MdtTheme.color.surfaceContainer, RoundedShapeIndexed(isFirst, isLast))
            .padding(start = 16.dp, end = 0.dp, top = 16.dp, bottom = 16.dp)
            .animateContentSize(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row {
                    Text(
                        text = title,
                        style = MdtTheme.typo.titleSmall,
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MdtTheme.typo.bodyMedium,
                        color = MdtTheme.color.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            actions()
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            uiState = ManageSubscriptionUiState(
                subscriptionPlan = SubscriptionPlan.PreviewPaid,
            ),
            screenState = ScreenState.Success,
        )
    }
}