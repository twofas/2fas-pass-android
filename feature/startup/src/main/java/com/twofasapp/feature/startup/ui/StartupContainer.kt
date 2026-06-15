/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.twofasapp.core.android.navigation.Screen
import com.twofasapp.core.design.MdtTheme
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun StartupContainer(
    viewModel: StartupViewModel = koinViewModel(),
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.checkStartupDataValidity(
            onExpired = {
                navController.navigate(Screen.Welcome) {
                    popUpTo(0)
                }
            },
        )
        onPauseOrDispose { }
    }

    Content(
        navController = navController,
        uiState = uiState,
    )
}

@Composable
private fun Content(
    navController: NavHostController,
    uiState: StartupUiState,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MdtTheme.color.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        StartupNavHost(
            navController = navController,
            modifier = Modifier.fillMaxSize(),
        )
    }
}