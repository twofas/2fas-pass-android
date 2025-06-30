/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.vaultsetup.halfway

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.twofasapp.feature.startup.ui.vaultsetup.common.VaultSetupProgress
import com.twofasapp.feature.startup.ui.vaultsetup.common.VaultSetupProgressState
import kotlinx.coroutines.delay

@Composable
internal fun VaultSetupHalfWayScreen(
    openCreateMasterPassword: () -> Unit,
) {
    Content(
        onCtaClick = openCreateMasterPassword,
    )
}

@Composable
internal fun Content(
    onCtaClick: () -> Unit = {},
) {
    var ctaVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1200)
        ctaVisible = true
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
                title = MdtLocale.strings.setupVaultHalfWayTitle,
                description = MdtLocale.strings.setupVaultHalfWayDescription,
                image = painterResource(R.drawable.progress_shield_50),
            )

            Space(32.dp)

            VaultSetupProgress(
                modifier = Modifier.fillMaxWidth(),
                state = VaultSetupProgressState.HalfWay,
            )

            Space(1f)

            AnimatedFadeVisibility(
                visible = ctaVisible,
                modifier = Modifier.fillMaxWidth(),
                enter = fadeIn(tween(400)),
            ) {
                Button(
                    text = MdtLocale.strings.commonContinue,
                    onClick = onCtaClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
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