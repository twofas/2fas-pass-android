/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.ui.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.twofasapp.core.android.navigation.NavAnimation
import com.twofasapp.core.android.navigation.Screen
import com.twofasapp.core.android.viewmodel.ProvidesViewModelStoreOwner
import com.twofasapp.core.common.domain.SelectedTheme
import com.twofasapp.core.design.AppTheme
import com.twofasapp.core.design.LocalAppTheme
import com.twofasapp.core.design.LocalAuthStatus
import com.twofasapp.core.design.LocalDynamicColors
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.anim.AnimatedFadeVisibility
import com.twofasapp.feature.lock.navigation.LockRoute
import com.twofasapp.feature.main.navigation.MainRoute
import com.twofasapp.feature.startup.navigation.StartupRoute
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun AppContainer(
    viewModel: AppViewModel = koinViewModel(),
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CompositionLocalProvider(
        LocalAppTheme provides when (uiState.selectedTheme) {
            SelectedTheme.Auto -> AppTheme.Auto
            SelectedTheme.Light -> AppTheme.Light
            SelectedTheme.Dark -> AppTheme.Dark
        },
        LocalDynamicColors provides uiState.dynamicColors,
        LocalAuthStatus provides uiState.authStatus,
    ) {
        AppTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MdtTheme.color.background,
            ) {
                uiState.startDestination?.let { startDestination ->
                    NavHost(
                        navController = navController,
                        startDestination = when (startDestination) {
                            AppStartDestination.Startup -> Screen.StartupContainer
                            AppStartDestination.Main -> Screen.MainContainer
                        },
                        enterTransition = NavAnimation.Enter,
                        exitTransition = NavAnimation.Exit,
                    ) {
                        composable<Screen.StartupContainer> {
                            StartupRoute()
                        }

                        composable<Screen.MainContainer> {
                            MainRoute()
                        }
                    }
                }

                uiState.showLock?.let { showLockScreen ->
                    AnimatedFadeVisibility(showLockScreen) {
                        ProvidesViewModelStoreOwner {
                            LockRoute()
                        }
                    }
                }
            }
        }
    }
}