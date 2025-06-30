/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.twofasapp.core.android.navigation.NavAnimation
import com.twofasapp.core.android.navigation.Screen
import com.twofasapp.feature.startup.navigation.CreateDecryptionKitRoute
import com.twofasapp.feature.startup.navigation.CreateMasterPasswordRoute
import com.twofasapp.feature.startup.navigation.CreateSecretKeyRoute
import com.twofasapp.feature.startup.navigation.CreateSecretKeySuccessRoute
import com.twofasapp.feature.startup.navigation.DecryptVaultRoute
import com.twofasapp.feature.startup.navigation.RestoreCloudFilesRoute
import com.twofasapp.feature.startup.navigation.RestoreVaultRoute
import com.twofasapp.feature.startup.navigation.RestoreWebDavRoute
import com.twofasapp.feature.startup.navigation.VaultSetupCompletedRoute
import com.twofasapp.feature.startup.navigation.VaultSetupHalfWayRoute
import com.twofasapp.feature.startup.navigation.VaultSetupStartRoute
import com.twofasapp.feature.startup.navigation.WelcomeRoute

@Composable
internal fun StartupNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Welcome,
        enterTransition = NavAnimation.Enter,
        exitTransition = NavAnimation.Exit,
        modifier = modifier,
    ) {
        composable<Screen.Welcome> {
            WelcomeRoute(
                openStartVault = { navController.navigate(Screen.VaultSetupStart) },
                openRestoreVault = { navController.navigate(Screen.RestoreVault) },
            )
        }

        composable<Screen.VaultSetupStart> {
            VaultSetupStartRoute(
                openCreateVault = { navController.navigate(Screen.CreateSecretKey) },
            )
        }

        composable<Screen.VaultSetupHalfWay> {
            VaultSetupHalfWayRoute(
                openCreateMasterPassword = { navController.navigate(Screen.CreateMasterPassword) },
            )
        }

        composable<Screen.VaultSetupCompleted> {
            VaultSetupCompletedRoute()
        }

        composable<Screen.CreateSecretKey> {
            CreateSecretKeyRoute(
                openSecretKeySuccess = { navController.navigate(Screen.CreateSecretKeySuccess) },
            )
        }

        composable<Screen.CreateSecretKeySuccess> {
            CreateSecretKeySuccessRoute(
                openCreateMasterPassword = {
                    navController.navigate(Screen.VaultSetupHalfWay) {
                        popUpTo<Screen.CreateSecretKey>()
                    }
                },
            )
        }

        composable<Screen.CreateMasterPassword> {
            CreateMasterPasswordRoute(
                openNextStep = { navController.navigate(Screen.CreateDecryptionKit) },
            )
        }

        composable<Screen.CreateDecryptionKit> {
            CreateDecryptionKitRoute(
                openNextStep = { navController.navigate(Screen.VaultSetupCompleted) },
            )
        }

        composable<Screen.RestoreVault> {
            RestoreVaultRoute(
                openRestoreCloudFiles = { navController.navigate(Screen.RestoreCloudFiles) },
                openRestoreWebDavConfig = { navController.navigate(Screen.RestoreWebDav) },
                openDecryptVault = { navController.navigate(Screen.DecryptVault) },
            )
        }

        composable<Screen.RestoreWebDav> {
            RestoreWebDavRoute(
                openRestoreCloudFiles = { navController.navigate(Screen.RestoreCloudFiles) },
            )
        }

        composable<Screen.RestoreCloudFiles> {
            RestoreCloudFilesRoute(
                openDecryptVault = { navController.navigate(Screen.DecryptVault) },
            )
        }

        composable<Screen.DecryptVault> {
            DecryptVaultRoute()
        }
    }
}