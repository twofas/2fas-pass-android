/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.main.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.twofasapp.core.android.ktx.navigateTopLevel
import com.twofasapp.core.android.navigation.NavAnimation
import com.twofasapp.core.android.navigation.Screen
import com.twofasapp.feature.cloudsync.navigation.GoogleDriveSyncRoute
import com.twofasapp.feature.cloudsync.navigation.WebDavSyncRoute
import com.twofasapp.feature.connect.navigation.ConnectRoute
import com.twofasapp.feature.developer.navigation.DeveloperRoute
import com.twofasapp.feature.externalimport.navigation.ExternalImportRoute
import com.twofasapp.feature.home.navigation.EditLoginRoute
import com.twofasapp.feature.home.navigation.HomeRoute
import com.twofasapp.feature.settings.navigation.AboutRoute
import com.twofasapp.feature.settings.navigation.AutofillRoute
import com.twofasapp.feature.settings.navigation.ChangePasswordRoute
import com.twofasapp.feature.settings.navigation.CloudSyncRoute
import com.twofasapp.feature.settings.navigation.CustomizationRoute
import com.twofasapp.feature.settings.navigation.ImportExportRoute
import com.twofasapp.feature.settings.navigation.KnownBrowsersRoute
import com.twofasapp.feature.settings.navigation.LockoutSettingsRoute
import com.twofasapp.feature.settings.navigation.ManageSubscriptionRoute
import com.twofasapp.feature.settings.navigation.OpenSourceLibrariesRoute
import com.twofasapp.feature.settings.navigation.ProcessingNewPasswordRoute
import com.twofasapp.feature.settings.navigation.ProtectionLevelRoute
import com.twofasapp.feature.settings.navigation.PushNotificationsRoute
import com.twofasapp.feature.settings.navigation.SaveDecryptionKitRoute
import com.twofasapp.feature.settings.navigation.SecurityRoute
import com.twofasapp.feature.settings.navigation.SetNewPasswordRoute
import com.twofasapp.feature.settings.navigation.SettingsRoute
import com.twofasapp.feature.settings.navigation.TransferFromOtherAppsRoute
import com.twofasapp.feature.settings.navigation.TrashRoute

@Composable
internal fun MainNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home(),
        enterTransition = NavAnimation.Enter,
        exitTransition = NavAnimation.Exit,
        modifier = modifier,
    ) {
        composable<Screen.Home> {
            HomeRoute(
                openAddLogin = { vaultId ->
                    navController.navigate(
                        Screen.EditLogin(
                            loginId = "",
                            vaultId = vaultId,
                        ),
                    )
                },
                openEditLogin = { loginId, vaultId ->
                    navController.navigate(
                        Screen.EditLogin(
                            loginId = loginId,
                            vaultId = vaultId,
                        ),
                    )
                },
                openSettings = {
                    navController.navigateTopLevel(Screen.Settings())
                },
                openDeveloper = {
                    navController.navigateTopLevel(Screen.Developer)
                },
            )
        }

        composable<Screen.Connect> {
            ConnectRoute(
                onOpenHome = {
                    navController.popBackStack<Screen.Home>(false)
                    navController.navigateTopLevel(Screen.Home())
                },
                onGoBack = { navController.popBackStack() },
            )
        }

        composable<Screen.Settings> {
            SettingsRoute()
        }

        composable<Screen.Developer> {
            DeveloperRoute()
        }

        composable<Screen.EditLogin> {
            EditLoginRoute(
                close = { navController.popBackStack() },
            )
        }

        composable<Screen.Security> {
            SecurityRoute()
        }

        composable<Screen.ChangePassword> {
            ChangePasswordRoute(
                openSetNewPassword = { navController.navigate(Screen.SetNewPassword) },
            )
        }

        composable<Screen.SetNewPassword> {
            SetNewPasswordRoute(
                openProcessingNewPassword = { password ->
                    navController.navigate(
                        Screen.ProcessingNewPassword(encryptedPassword = password),
                    ) {
                        popUpTo<Screen.Security>()
                    }
                },
            )
        }

        composable<Screen.ProcessingNewPassword> {
            ProcessingNewPasswordRoute(
                onOpenDecryptionKit = { keyHex ->
                    navController.navigate(Screen.SaveDecryptionKit(masterKeyHex = keyHex)) {
                        popUpTo<Screen.Security>()
                    }
                },
                onClose = { navController.popBackStack() },
            )
        }

        composable<Screen.LockoutSettings> {
            LockoutSettingsRoute()
        }

        composable<Screen.SaveDecryptionKit> {
            SaveDecryptionKitRoute()
        }

        composable<Screen.ProtectionLevel> {
            ProtectionLevelRoute()
        }

        composable<Screen.Autofill> {
            AutofillRoute()
        }

        composable<Screen.Customization> {
            CustomizationRoute()
        }

        composable<Screen.KnownBrowsers> {
            KnownBrowsersRoute()
        }

        composable<Screen.PushNotifications> {
            PushNotificationsRoute()
        }

        composable<Screen.CloudSync> {
            CloudSyncRoute()
        }

        composable<Screen.GoogleDriveSync> {
            GoogleDriveSyncRoute(
                goBackToSync = {
                    if (navController.popBackStack<Screen.CloudSync>(false).not()) {
                        navController.popBackStack()
                        navController.navigate(Screen.CloudSync)
                    }
                },
                goBackToSettings = { navController.popBackStack<Screen.Settings>(false) },
            )
        }

        composable<Screen.WebDavSync> {
            WebDavSyncRoute(
                goBackToSync = {
                    if (navController.popBackStack<Screen.CloudSync>(false).not()) {
                        navController.popBackStack()
                        navController.navigate(Screen.CloudSync)
                    }
                },
                goBackToSettings = { navController.popBackStack<Screen.Settings>(false) },
            )
        }

        composable<Screen.ImportExport> {
            ImportExportRoute(
                openLogins = {
                    navController.popBackStack<Screen.Settings>(false)
                    navController.navigateTopLevel(Screen.Home())
                },
            )
        }

        composable<Screen.TransferFromOtherApps> {
            TransferFromOtherAppsRoute()
        }

        composable<Screen.ExternalImport> {
            ExternalImportRoute(
                openLogins = {
                    navController.popBackStack<Screen.Settings>(false)
                    navController.navigateTopLevel(Screen.Home())
                },
            )
        }

        composable<Screen.Trash> {
            TrashRoute()
        }

        composable<Screen.About> {
            AboutRoute()
        }

        composable<Screen.OpenSourceLibraries> {
            OpenSourceLibrariesRoute()
        }

        composable<Screen.ManageSubscription> {
            ManageSubscriptionRoute()
        }
    }
}