/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.welcome

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.RenderMode
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.headers.ScreenHeader
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonStyle
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.text.TextIcon
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.RoundedShape16
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.permissions.isGranted
import com.twofasapp.feature.permissions.rememberPermissionState
import com.twofasapp.feature.startup.R
import com.twofasapp.feature.startup.R.drawable.feature_all_inclusive
import com.twofasapp.feature.startup.R.drawable.feature_link
import com.twofasapp.feature.startup.R.drawable.feature_maze
import com.twofasapp.feature.startup.R.drawable.feature_person_off
import com.twofasapp.feature.startup.R.drawable.feature_phonelink_lock
import com.twofasapp.feature.startup.R.drawable.feature_security
import com.twofasapp.feature.startup.R.drawable.feature_share
import com.twofasapp.feature.startup.R.drawable.feature_visibility_off
import com.twofasapp.feature.startup.R.drawable.feature_vpn_key_off
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun WelcomeScreen(
    viewModel: WelcomeViewModel = koinViewModel(),
    openStartVault: () -> Unit,
    openRestoreVault: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        onStartClick = openStartVault,
        onRestoreClick = openRestoreVault,
        onSkipClick = { viewModel.devSkip() },
    )
}

@Composable
internal fun Content(
    uiState: WelcomeUiState,
    onStartClick: () -> Unit = {},
    onRestoreClick: () -> Unit = {},
    onSkipClick: () -> Unit = {},
) {
    if (uiState.debuggable) {
        // Ask for POST_NOTIFICATIONS permission only on debug builds - it's needed for Pluto plugin
        AskForNotificationsPermissions()
    }

    val strings = MdtLocale.strings
    val pagerState = rememberPagerState(pageCount = { 3 })
    val composition1 by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.onboarding_01))
    val composition2 by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.onboarding_02))
    val composition3 by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.onboarding_03))

    Scaffold(
        topBar = {
            TopAppBar(
                showBackButton = false,
                actions = {
                    if (uiState.debuggable) {
                        Button(
                            text = "[DEV] Skip",
                            style = ButtonStyle.Text,
                            onClick = onSkipClick,
                        )
                    }
                },
            )
        },
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(top = padding.calculateTopPadding(), bottom = ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HorizontalPager(
                state = pagerState,
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ScreenPadding),
                ) {
                    LottieAnimation(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.45f)
                            .padding(horizontal = 24.dp)
                            .clip(RoundedShape16)
                            .background(MdtTheme.color.surfaceContainer),
                        composition = when (page) {
                            0 -> composition1
                            1 -> composition2
                            2 -> composition3
                            else -> composition1
                        },
                        iterations = LottieConstants.IterateForever,
                        alignment = Alignment.BottomCenter,
                        renderMode = RenderMode.HARDWARE,
                    )

                    Space(24.dp)

                    ScreenHeader(
                        modifier = Modifier.fillMaxWidth(),
                        title = when (page) {
                            0 -> strings.onboardingWelcome1Title
                            1 -> strings.onboardingWelcome2Title
                            2 -> strings.onboardingWelcome3Title
                            else -> ""
                        },
                        description = when (page) {
                            0 -> strings.onboardingWelcome1Description
                            1 -> strings.onboardingWelcome2Description
                            2 -> strings.onboardingWelcome3Description
                            else -> ""
                        },
                    )

                    Space(24.dp)

                    Features(
                        modifier = Modifier.fillMaxWidth(),
                        features = when (page) {
                            0 -> listOf(
                                feature_security to strings.onboardingWelcome1Feature1,
                                feature_visibility_off to strings.onboardingWelcome1Feature2,
                                feature_person_off to strings.onboardingWelcome1Feature3,
                            )

                            1 -> listOf(
                                feature_all_inclusive to strings.onboardingWelcome2Feature1,
                                feature_share to strings.onboardingWelcome2Feature2,
                                feature_vpn_key_off to strings.onboardingWelcome2Feature3,
                            )

                            2 -> listOf(
                                feature_link to strings.onboardingWelcome3Feature1,
                                feature_phonelink_lock to strings.onboardingWelcome3Feature2,
                                feature_maze to strings.onboardingWelcome3Feature3,
                            )

                            else -> emptyList()
                        },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == iteration) {
                                    MdtTheme.color.primary
                                } else {
                                    MdtTheme.color.surfaceContainerHighest
                                },
                            )
                            .size(8.dp),
                    )
                }
            }

            Space(16.dp)

            Button(
                text = strings.onboardingWelcomeCtaStart,
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenPadding),
            )

            Space(8.dp)

            Button(
                text = strings.onboardingWelcomeCtaRestore,
                style = ButtonStyle.Text,
                onClick = onRestoreClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenPadding),
            )
        }
    }
}

@Composable
private fun Features(
    modifier: Modifier = Modifier,
    features: List<Pair<Int, String>>,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        features.forEach { feature ->
            TextIcon(
                text = feature.second,
                style = MdtTheme.typo.bodyMedium,
                color = MdtTheme.color.onSurface,
                leadingIcon = painterResource(feature.first),
                leadingIconTint = MdtTheme.color.primary,
                leadingIconSize = 20.dp,
                leadingIconSpacer = 4.dp,
            )
        }
    }
}

@Composable
fun AskForNotificationsPermissions(onStateChange: (Boolean) -> Unit = {}) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permission = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)

        LaunchedEffect(Unit) {
            permission.launchPermissionRequest()
        }

        LaunchedEffect(permission.status.isGranted) {
            onStateChange(permission.status.isGranted)
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            uiState = WelcomeUiState(),
        )
    }
}