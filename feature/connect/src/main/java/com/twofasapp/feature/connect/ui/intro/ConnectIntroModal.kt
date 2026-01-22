/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.connect.ui.intro

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.RenderMode
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.twofasapp.core.android.ktx.openSafely
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.headers.ScreenHeader
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonHeight
import com.twofasapp.core.design.foundation.button.ButtonStyle
import com.twofasapp.core.design.foundation.layout.ZeroPadding
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.modal.ModalHeaderProperties
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.text.richText
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.design.theme.RoundedShape16
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.connect.R

private enum class ConnectIntroState {
    Intro,
    Permissions,
}

@Composable
internal fun ConnectIntroModal(
    onDismissRequest: () -> Unit = {},
) {
    var uiState by remember { mutableStateOf(ConnectIntroState.Intro) }

    BackHandler { uiState = ConnectIntroState.Intro }

    Modal(
        onDismissRequest = onDismissRequest,
        dismissOnSwipe = false,
        dismissOnBackPress = false,
        headerProperties = ModalHeaderProperties(
            showDragHandle = false,
            showCloseButton = true,
        ),
        containerColor = MdtTheme.color.background,
    ) { dismiss ->
        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220, delayMillis = 90)))
                    .togetherWith(fadeOut(animationSpec = tween(90)))
            },
        ) { state ->
            when (state) {
                ConnectIntroState.Intro -> {
                    IntroContent(
                        onDismissRequest = { uiState = ConnectIntroState.Permissions },
                    )
                }

                ConnectIntroState.Permissions -> {
                    PermissionsContent(
                        onDismissRequest = { dismiss { onDismissRequest() } },
                    )
                }
            }
        }
    }
}

@Composable
private fun IntroContent(
    onDismissRequest: () -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.onboarding_03))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        LottieAnimation(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .clip(RoundedShape16)
                .background(MdtTheme.color.surfaceContainer),
            composition = composition,
            iterations = LottieConstants.IterateForever,
            alignment = Alignment.BottomCenter,
            renderMode = RenderMode.HARDWARE,
        )

        Space(24.dp)

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = MdtLocale.strings.setupConnectIntroTitle,
            style = MdtTheme.typo.headlineSmall,
            color = MdtTheme.color.onSurface,
            textAlign = TextAlign.Center,
        )

        Space(12.dp)

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = richText(MdtLocale.strings.setupConnectIntroDescription),
            style = MdtTheme.typo.bodyMedium,
            color = MdtTheme.color.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Space(12.dp)

        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = MdtLocale.strings.setupConnectLearnMore,
            size = ButtonHeight.Small,
            style = ButtonStyle.Text,
            onClick = { uriHandler.openSafely(MdtLocale.links.browserExtension) },
        )

        Space(1f)

        Button(
            modifier = Modifier
                .fillMaxWidth(),
            text = MdtLocale.strings.commonContinue,
            onClick = onDismissRequest,
        )
    }
}

@Composable
private fun PermissionsContent(
    onDismissRequest: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
    ) {
        ScreenHeader(
            title = MdtLocale.strings.setupConnectTitle,
            description = MdtLocale.strings.setupConnectDescription,
            image = painterResource(R.drawable.progress_shield_100),
        )

        Space(24.dp)

        Box(
            modifier = Modifier
                .clip(RoundedShape12)
                .background(MdtTheme.color.surfaceContainer)
                .padding(16.dp),
        ) {
            OptionEntry(
                icon = MdtIcons.QrScanner,
                title = MdtLocale.strings.setupConnectStepCameraTitle,
                subtitle = MdtLocale.strings.setupConnectStepCameraDescription,
                contentPadding = ZeroPadding,
            )
        }

        Space(12.dp)

        Box(
            modifier = Modifier
                .clip(RoundedShape12)
                .background(MdtTheme.color.surfaceContainer)
                .padding(16.dp),
        ) {
            OptionEntry(
                icon = MdtIcons.Notifications,
                title = MdtLocale.strings.setupConnectStepNotificationsTitle,
                subtitle = MdtLocale.strings.setupConnectStepNotificationsDescription,
                contentPadding = ZeroPadding,
            )
        }

        Space(1f)

        Button(
            modifier = Modifier
                .fillMaxWidth(),
            text = MdtLocale.strings.connectPermissionsEnable,
            onClick = onDismissRequest,
        )
    }
}

@Preview
@Composable
private fun PreviewInfo() {
    PreviewTheme {
        IntroContent()
    }
}

@Preview
@Composable
private fun PreviewPermissions() {
    PreviewTheme {
        PermissionsContent()
    }
}