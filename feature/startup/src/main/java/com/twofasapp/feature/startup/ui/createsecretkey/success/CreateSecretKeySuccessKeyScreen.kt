/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.createsecretkey.success

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.RenderMode
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.anim.AnimatedFadeVisibility
import com.twofasapp.core.design.feature.headers.ScreenHeader
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.startup.R
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun CreateSecretKeySuccessScreen(
    viewModel: CreateSecretKeySuccessViewModel = koinViewModel(),
    openCreateMasterPassword: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        onCtaClick = openCreateMasterPassword,
    )
}

@Composable
internal fun Content(
    uiState: CreateSecretKeySuccessUiState,
    onCtaClick: () -> Unit = {},
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.secret_key_tick))
    var animSize by remember { mutableStateOf(false) }
    var animTick by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (animSize) 0.4f else 1f,
        animationSpec = tween(durationMillis = 800),
    )
    val borderScale by animateFloatAsState(
        targetValue = if (animSize) 0.7f else 1f,
        animationSpec = tween(durationMillis = 800),
    )

    LaunchedEffect(Unit) {
        awaitFrame()
        animSize = true
        delay(600)
        animTick = true
    }

    Scaffold(
        topBar = { TopAppBar() },
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(top = padding.calculateTopPadding(), bottom = ScreenPadding)
                .padding(horizontal = ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ScreenHeader(
                title = MdtLocale.strings.generateSecretKeySuccessTitle,
                description = MdtLocale.strings.generateSecretKeySuccessDescription,
                image = painterResource(R.drawable.progress_shield_25),
            )

            Space(1f)
            Box(
                contentAlignment = Alignment.Center,

                modifier = Modifier
                    .size(255.dp * scale)
                    .border(width = 5.dp * borderScale, color = MdtTheme.color.primary, shape = CircleShape),
            ) {
                AnimatedFadeVisibility(
                    visible = animTick,
                    modifier = Modifier.size(50.dp),
                    enter = fadeIn(tween(200)),
                ) {
                    LottieAnimation(
                        modifier = Modifier.size(50.dp),
                        contentScale = ContentScale.Crop,
                        composition = composition,
                        iterations = 1,
                        renderMode = RenderMode.HARDWARE,
                    )
                }
            }

            Space(1f)

            Button(
                text = MdtLocale.strings.commonContinue,
                modifier = Modifier.fillMaxWidth(),
                onClick = onCtaClick,
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            uiState = CreateSecretKeySuccessUiState(),
        )
    }
}