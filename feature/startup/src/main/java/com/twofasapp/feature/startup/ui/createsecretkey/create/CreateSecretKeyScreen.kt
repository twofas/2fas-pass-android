/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.createsecretkey.create

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.RenderMode
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.headers.ScreenHeader
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.startup.R
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun CreateSecretKeyScreen(
    viewModel: CreateSecretKeyViewModel = koinViewModel(),
    openSecretKeySuccess: () -> Unit,
) {
    Content(
        onDone = {
            viewModel.generateSeed {
                openSecretKeySuccess()
            }
        },
    )
}

@Composable
internal fun Content(
    onDone: () -> Unit = {},
) {
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.secret_key_shuffle))

    val pressThreshold = 1000L
    var pressed by remember { mutableStateOf(false) }
    val pressProgress: Float by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (pressed) pressThreshold.toInt() else 100,
            easing = LinearEasing,
        ),
        label = "pressProgress",
    )
    val pressProgressDone by remember {
        derivedStateOf {
            pressProgress == 1f
        }
    }

    LaunchedEffect(pressProgressDone) {
        if (pressProgressDone) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onDone()
        }
    }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    pressed = true
                }

                is PressInteraction.Release -> {
                    if (pressProgress < 1f) {
                        pressed = false
                    }
                }

                is PressInteraction.Cancel -> {
                    if (pressProgress < 1f) {
                        pressed = false
                    }
                }
            }
        }
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
                title = MdtLocale.strings.generateSecretKeyTitle,
                description = MdtLocale.strings.generateSecretKeyDescription,
                image = painterResource(R.drawable.progress_shield_25),
            )

            Space(1f)

            Box(
                modifier = Modifier.scale(1f + pressProgress * 0.02f),
                contentAlignment = Alignment.Center,
            ) {
                LottieAnimation(
                    modifier = Modifier.size(320.dp),
                    contentScale = ContentScale.Crop,
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    speed = if (pressed) 2f else 0.5f,
                    renderMode = RenderMode.HARDWARE,
                )

                CircularProgressIndicator(
                    modifier = Modifier.size(250.dp),
                    color = MdtTheme.color.primary,
                    strokeWidth = 5.dp,
                    trackColor = MdtTheme.color.surfaceContainerHighest,
                    strokeCap = StrokeCap.Butt,
                    progress = { pressProgress },
                )
            }

            Space(1f)

            Button(
                text = MdtLocale.strings.generateSecretKeyCta,
                modifier = Modifier.fillMaxWidth(),
                interactionSource = interactionSource,
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content()
    }
}