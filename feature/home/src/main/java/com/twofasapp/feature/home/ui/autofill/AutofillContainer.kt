/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.autofill

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
import com.twofasapp.feature.home.navigation.EditItemRoute
import com.twofasapp.feature.home.ui.autofill.auth.AutofillAuthScreen
import com.twofasapp.feature.home.ui.autofill.picker.AutofillPickerScreen
import com.twofasapp.feature.home.ui.autofill.save.AutofillSaveLoginScreen
import com.twofasapp.feature.lock.navigation.LockRoute
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun AutofillContainer(
    viewModel: AutofillViewModel = koinViewModel(),
    startScreen: AutofillActivity.StartScreen,
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
                when (startScreen) {
                    AutofillActivity.StartScreen.Authenticate -> {
                        AutofillAuthScreen()
                    }

                    AutofillActivity.StartScreen.PickLogin -> {
                        uiState.showLock?.let { showLock ->
                            if (showLock) {
                                AnimatedFadeVisibility(showLock) {
                                    ProvidesViewModelStoreOwner {
                                        LockRoute()
                                    }
                                }
                            } else {
                                NavHost(
                                    navController = navController,
                                    startDestination = Screen.AutofillPicker,
                                    enterTransition = NavAnimation.Enter,
                                    exitTransition = NavAnimation.Exit,
                                ) {
                                    composable<Screen.AutofillPicker> {
                                        AutofillPickerScreen(
                                            openAddLogin = { navController.navigate(Screen.AutofillSaveLogin) },
                                            openEditLogin = { item ->
                                                navController.navigate(
                                                    Screen.EditItem(
                                                        itemId = item.id,
                                                        vaultId = item.vaultId,
                                                        itemContentTypeKey = item.contentType.key,
                                                    ),
                                                )
                                            },
                                        )
                                    }

                                    composable<Screen.AutofillSaveLogin> {
                                        AutofillSaveLoginScreen()
                                    }

                                    composable<Screen.EditItem> {
                                        EditItemRoute(
                                            close = { navController.popBackStack() },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    AutofillActivity.StartScreen.SaveLogin -> {
                        uiState.showLock?.let { showLock ->
                            if (showLock) {
                                AnimatedFadeVisibility(showLock) {
                                    ProvidesViewModelStoreOwner {
                                        LockRoute()
                                    }
                                }
                            } else {
                                AutofillSaveLoginScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}