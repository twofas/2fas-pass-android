/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.vaultsetup.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.AppTheme
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.anim.AnimatedFadeVisibility
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewColumn
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.design.theme.RoundedShape16
import com.twofasapp.core.locale.MdtLocale
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay

enum class VaultSetupProgressState {
    Start, HalfWay, Completed,
}

@Composable
internal fun VaultSetupProgress(
    modifier: Modifier = Modifier,
    state: VaultSetupProgressState = VaultSetupProgressState.Start,
) {
    var firstItemEnabled by rememberSaveable {
        mutableStateOf(
            when (state) {
                VaultSetupProgressState.Start -> true
                VaultSetupProgressState.HalfWay -> true
                VaultSetupProgressState.Completed -> false
            },
        )
    }
    var firstBorderVisible by rememberSaveable { mutableStateOf(false) }
    val firstBorderColor by borderAnimation(firstBorderVisible)
    val firstAlpha by alphaAnimation(firstItemEnabled)

    var secondItemEnabled by rememberSaveable { mutableStateOf(true) }
    var secondBorderVisible by rememberSaveable { mutableStateOf(false) }
    val secondBorderColor by borderAnimation(secondBorderVisible)
    val secondAlpha by alphaAnimation(secondItemEnabled && firstItemEnabled.not())

    LaunchedEffect(Unit) {
        awaitFrame()

        when (state) {
            VaultSetupProgressState.Start -> {
                delay(100)
                firstItemEnabled = true
                firstBorderVisible = true

                secondItemEnabled = true
                secondBorderVisible = false
            }

            VaultSetupProgressState.HalfWay -> {
                delay(200)
                firstItemEnabled = false
                firstBorderVisible = false

                delay(400)
                secondItemEnabled = true
                secondBorderVisible = true
            }

            VaultSetupProgressState.Completed -> {
                delay(200)
                firstItemEnabled = false
                firstBorderVisible = false
                secondItemEnabled = false
                secondBorderVisible = false
            }
        }
    }

    Column(
        modifier = modifier
            .background(MdtTheme.color.surfaceContainer, RoundedShape16)
            .padding(8.dp),
        horizontalAlignment = CenterHorizontally,
    ) {
        Text(
            text = MdtLocale.strings.setupVaultHeader,
            style = MdtTheme.typo.bodyMedium,
            color = MdtTheme.color.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        Space(12.dp)

        Column(
            modifier = Modifier
                .background(MdtTheme.color.surface, RoundedShape12)
                .border(width = 3.dp, color = firstBorderColor, shape = RoundedShape12)
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(24.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = MdtLocale.strings.setupGenerateSecretKeyTitle,
                    style = when (state) {
                        VaultSetupProgressState.Start -> MdtTheme.typo.titleMedium
                        VaultSetupProgressState.HalfWay -> MdtTheme.typo.titleMedium.copy(textDecoration = TextDecoration.LineThrough)
                        VaultSetupProgressState.Completed -> MdtTheme.typo.titleMedium.copy(textDecoration = TextDecoration.LineThrough)
                    },
                    color = MdtTheme.color.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .alpha(firstAlpha),
                )

                AnimatedFadeVisibility(
                    visible = firstItemEnabled.not(),
                    modifier = Modifier.offset(x = 4.dp),
                ) {
                    Icon(
                        painter = MdtIcons.CircleCheckThinFilled,
                        contentDescription = null,
                        tint = MdtTheme.color.primary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Space(4.dp)

            Text(
                text = MdtLocale.strings.setupGenerateSecretKeyDescription,
                style = MdtTheme.typo.bodyMedium,
                color = MdtTheme.color.onSurfaceVariant,
                modifier = Modifier.alpha(firstAlpha),
            )
        }

        Space(8.dp)

        Column(
            modifier = Modifier
                .background(MdtTheme.color.surface, RoundedShape12)
                .border(width = 3.dp, color = secondBorderColor, shape = RoundedShape12)
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(24.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = MdtLocale.strings.setupCreateMasterPasswordTitle,
                    style = when (state) {
                        VaultSetupProgressState.Start -> MdtTheme.typo.titleMedium
                        VaultSetupProgressState.HalfWay -> MdtTheme.typo.titleMedium
                        VaultSetupProgressState.Completed -> MdtTheme.typo.titleMedium.copy(textDecoration = TextDecoration.LineThrough)
                    },
                    color = MdtTheme.color.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .alpha(secondAlpha),
                )

                AnimatedFadeVisibility(
                    visible = secondItemEnabled.not(),
                    modifier = Modifier.offset(x = 4.dp),
                ) {
                    Icon(
                        painter = MdtIcons.CircleCheckThinFilled,
                        contentDescription = null,
                        tint = MdtTheme.color.primary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Space(4.dp)

            Text(
                text = MdtLocale.strings.setupCreateMasterPasswordDescription,
                style = MdtTheme.typo.bodyMedium,
                color = MdtTheme.color.onSurfaceVariant,
                modifier = Modifier.alpha(secondAlpha),
            )
        }
    }
}

@Composable
private fun borderAnimation(visible: Boolean) = animateColorAsState(
    targetValue = if (visible) MdtTheme.color.secondaryContainer else Color.Transparent,
    animationSpec = tween(durationMillis = 800),
)

@Composable
private fun alphaAnimation(visible: Boolean) = animateFloatAsState(
    targetValue = if (visible) 1f else 0.38f,
    animationSpec = tween(durationMillis = 400),
)

@Preview
@Composable
private fun PreviewDark() {
    PreviewColumn(theme = AppTheme.Dark) {
        VaultSetupProgress(
            modifier = Modifier.fillMaxWidth(),
            state = VaultSetupProgressState.Start,
        )

        VaultSetupProgress(
            modifier = Modifier.fillMaxWidth(),
            state = VaultSetupProgressState.HalfWay,
        )

        VaultSetupProgress(
            modifier = Modifier.fillMaxWidth(),
            state = VaultSetupProgressState.Completed,
        )
    }
}

@Preview
@Composable
private fun PreviewLight() {
    PreviewColumn(theme = AppTheme.Light) {
        VaultSetupProgress(
            modifier = Modifier.fillMaxWidth(),
            state = VaultSetupProgressState.Start,
        )

        VaultSetupProgress(
            modifier = Modifier.fillMaxWidth(),
            state = VaultSetupProgressState.HalfWay,
        )

        VaultSetupProgress(
            modifier = Modifier.fillMaxWidth(),
            state = VaultSetupProgressState.Completed,
        )
    }
}