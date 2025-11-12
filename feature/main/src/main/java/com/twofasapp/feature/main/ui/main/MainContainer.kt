/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.main.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.twofasapp.core.android.deeplinks.Deeplink
import com.twofasapp.core.android.ktx.navigateTopLevel
import com.twofasapp.core.android.ktx.openSafely
import com.twofasapp.core.android.navigation.ScreenType
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.dialog.InfoDialog
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.main.domain.BrowserRequestData
import com.twofasapp.data.main.domain.ConnectData
import com.twofasapp.feature.connect.ui.connectmodal.ConnectModal
import com.twofasapp.feature.connect.ui.requestmodal.RequestModal
import com.twofasapp.feature.purchases.PurchasesDialog
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@Composable
internal fun MainContainer(
    viewModel: MainViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        onResume = {
            viewModel.startObservingLocalPushes()
            viewModel.fetchBrowserRequests()
            viewModel.fetchSubscriptionInfo()
            viewModel.sync()
        },
        onPause = {
            viewModel.stopObservingLocalPushes()
            viewModel.sync()
        },
        markAppUpdatePrompted = {
            viewModel.markAppUpdatePrompted()
        },
        onEventConsumed = { viewModel.consumeEvent(it) },
    )
}

@Composable
private fun Content(
    uiState: MainUiState,
    onResume: () -> Unit = {},
    onPause: () -> Unit = {},
    markAppUpdatePrompted: () -> Unit = {},
    onEventConsumed: (MainUiEvent) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val uriHandler = LocalUriHandler.current
    var currentScreenRoute by remember { mutableStateOf<String?>(null) }
    var bottomBarVisible by remember { mutableStateOf(false) }
    var homeInEditMode by remember { mutableStateOf(false) }
    var browserConnectData by remember { mutableStateOf<ConnectData?>(null) }
    var browserRequestData by remember { mutableStateOf<BrowserRequestData?>(null) }
    var showPaywall by remember { mutableStateOf(false) }
    var showAppUpdateDialog by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        onResume()
        onPauseOrDispose { onPause() }
    }

    LaunchedEffect(uiState.events) {
        uiState.events.firstOrNull()?.let { uiEvent ->
            when (uiEvent) {
                is MainUiEvent.OpenDeeplink -> {
                    when (val deeplink = uiEvent.deeplink) {
                        is Deeplink.ToScreen -> {
                            scope.launch {
                                deeplink.screens.forEach { screen ->
                                    Timber.tag("NavController").d("[Deeplink.ToScreen] currentRoute=$currentScreenRoute, navigateTo=${screen.route}")

                                    when (screen.screenType) {
                                        ScreenType.Standard -> navController.navigate(screen)
                                        ScreenType.WithBottomBar -> navController.navigate(screen)
                                        ScreenType.TopLevel -> navController.navigateTopLevel(screen)
                                    }
                                }
                            }
                        }
                    }
                }

                is MainUiEvent.ShowBrowserConnect -> {
                    browserConnectData = uiEvent.browserConnectData
                }

                is MainUiEvent.ShowBrowserRequest -> {
                    browserRequestData = uiEvent.browserRequestData
                }

                is MainUiEvent.ShowAppUpdateDialog -> {
                    showAppUpdateDialog = true
                }
            }

            onEventConsumed(uiEvent)
        }
    }

    NavHandler(
        navController = navController,
        onCurrentRouteChanged = { currentScreenRoute = it },
        onBottomBarVisibilityChanged = { bottomBarVisible = it },
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MdtTheme.color.background)
            .then(if (bottomBarVisible) Modifier else Modifier.navigationBarsPadding()),
    ) {
        MainNavHost(
            navController = navController,
            modifier = Modifier.weight(1f),
            onHomeInEditModeChanged = { homeInEditMode = it },
        )

        AnimatedVisibility(
            visible = bottomBarVisible && homeInEditMode.not(),
            enter = slideInVertically { it } + expandVertically(expandFrom = Alignment.Bottom),
            exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + slideOutVertically { it },
        ) {
            MainBottomBar(
                navController = navController,
                cloudSyncError = uiState.cloudSyncError,
            )
        }
    }

    if (browserConnectData != null) {
        ConnectModal(
            connectData = browserConnectData!!,
            onDismissRequest = { browserConnectData = null },
            onUpgradePlan = { showPaywall = true },
        )
    }

    if (browserRequestData != null) {
        RequestModal(
            requestData = browserRequestData!!,
            onDismissRequest = { browserRequestData = null },
            onUpgradePlan = { showPaywall = true },
        )
    }

    if (showPaywall) {
        PurchasesDialog(
            onDismissRequest = { showPaywall = false },
        )
    }

    if (showAppUpdateDialog) {
        InfoDialog(
            onDismissRequest = { showAppUpdateDialog = false },
            icon = MdtIcons.Upgrade,
            title = MdtLocale.strings.appUpdateModalTitle,
            body = MdtLocale.strings.appUpdateModalSubtitle,
            negative = MdtLocale.strings.appUpdateModalCtaNegative,
            positive = MdtLocale.strings.appUpdateModalCtaPositive,
            onNegative = {
                markAppUpdatePrompted()
            },
            onPositive = {
                markAppUpdatePrompted()
                uriHandler.openSafely(MdtLocale.links.playStore)
            },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    Content(
        uiState = MainUiState(),
    )
}