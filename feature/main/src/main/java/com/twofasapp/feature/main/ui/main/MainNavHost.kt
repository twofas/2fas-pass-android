/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.main.ui.main

import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.twofasapp.core.android.ktx.navigateTopLevel
import com.twofasapp.core.android.navigation.NavAnimation
import com.twofasapp.core.android.navigation.Screen
import com.twofasapp.feature.cloudsync.navigation.S3SyncRoute
import com.twofasapp.feature.cloudsync.navigation.WebDavSyncRoute
import com.twofasapp.feature.connect.navigation.ConnectRoute
import com.twofasapp.feature.developer.navigation.DeveloperRoute
import com.twofasapp.feature.externalimport.navigation.ExternalImportRoute
import com.twofasapp.feature.home.navigation.EditItemRoute
import com.twofasapp.feature.home.navigation.HomeRoute
import com.twofasapp.feature.home.navigation.ItemDetailsRoute
import com.twofasapp.feature.quicksetup.ui.QuickSetupRoute
import com.twofasapp.feature.settings.navigation.AboutRoute
import com.twofasapp.feature.settings.navigation.AutofillRoute
import com.twofasapp.feature.settings.navigation.CloudSyncRoute
import com.twofasapp.feature.settings.navigation.CustomizationRoute
import com.twofasapp.feature.settings.navigation.ImportExportRoute
import com.twofasapp.feature.settings.navigation.KnownBrowsersRoute
import com.twofasapp.feature.settings.navigation.LockoutSettingsRoute
import com.twofasapp.feature.settings.navigation.LogsRoute
import com.twofasapp.feature.settings.navigation.ManageSubscriptionRoute
import com.twofasapp.feature.settings.navigation.ManageTagsRoute
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
import com.twofasapp.feature.share.navigation.ShareLinkHandlerRoute

@Composable
internal fun MainNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onHomeInEditModeChanged: (Boolean) -> Unit,
    onHomeScrollingUpChanged: (Boolean) -> Unit,
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
                openAddItem = { vaultId, itemContentType ->
                    navController.navigate(
                        Screen.EditItem(
                            itemId = "",
                            vaultId = vaultId,
                            itemContentTypeKey = itemContentType.key,
                        ),
                    )
                },
                openEditItem = { itemId, vaultId, itemContentType ->
                    navController.navigate(
                        Screen.EditItem(
                            itemId = itemId,
                            vaultId = vaultId,
                            itemContentTypeKey = itemContentType.key,
                        ),
                    )
                },
                openItemDetails = { itemId, vaultId ->
                    navController.navigate(Screen.ItemDetails(itemId = itemId, vaultId = vaultId))
                },
                openManageTags = {
                    navController.navigateTopLevel(Screen.ManageTags)
                },
                openQuickSetup = {
                    navController.navigate(Screen.QuickSetup)
                },
                openDeveloper = {
                    navController.navigateTopLevel(Screen.Developer)
                },
                onHomeInEditModeChanged = onHomeInEditModeChanged,
                onHomeScrollingUpChanged = onHomeScrollingUpChanged,
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

        composable<Screen.QuickSetup>(
            enterTransition = { slideInVertically { it } + expandVertically(expandFrom = Alignment.Bottom) },
            exitTransition = { shrinkVertically(shrinkTowards = Alignment.Bottom) + slideOutVertically { it } },
        ) {
            QuickSetupRoute(
                openAutofill = { navController.navigate(Screen.Autofill) },
                openSync = { navController.navigate(Screen.CloudSync) },
                openSecurityType = { navController.navigate(Screen.SecurityType) },
                openImport = { navController.navigate(Screen.ImportExport) },
                openTransfer = { navController.navigate(Screen.TransferFromOtherApps) },
                close = { navController.popBackStack() },
            )
        }

        composable<Screen.Developer> {
            DeveloperRoute()
        }

        composable<Screen.EditItem> {
            EditItemRoute(
                close = { navController.popBackStack() },
            )
        }

        composable<Screen.ShareLinkHandler> {
            ShareLinkHandlerRoute(
                onDecrypted = { shareId, itemContentTypeKey ->
                    navController.navigate(
                        Screen.EditItem(
                            vaultId = "",
                            itemId = "",
                            itemContentTypeKey = itemContentTypeKey,
                            shareId = shareId,
                        ),
                    ) {
                        popUpTo<Screen.ShareLinkHandler> { inclusive = true }
                    }
                },
                close = { navController.popBackStack() },
            )
        }

        composable<Screen.ItemDetails> {
            ItemDetailsRoute(
                openEditItem = { itemId, vaultId, itemContentType ->
                    navController.navigate(
                        Screen.EditItem(
                            itemId = itemId,
                            vaultId = vaultId,
                            itemContentTypeKey = itemContentType.key,
                        ),
                    )
                },
                close = { navController.popBackStack() },
            )
        }

        composable<Screen.Security> {
            SecurityRoute()
        }

        composable<Screen.SetNewPassword> {
            SetNewPasswordRoute(
                openProcessingNewPassword = { password ->
                    navController.navigate(
                        Screen.ProcessingNewPassword(encryptedPassword = password),
                    ) {
                        popUpTo<Screen.Home>()
                    }
                },
            )
        }

        composable<Screen.ProcessingNewPassword> {
            ProcessingNewPasswordRoute(
                onOpenDecryptionKit = { keyHex ->
                    navController.navigate(Screen.SaveDecryptionKit(masterKeyHex = keyHex)) {
                        popUpTo<Screen.Home>()
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

        composable<Screen.SecurityType> {
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

        composable<Screen.WebDavSync> {
            WebDavSyncRoute(
                goBackToSync = {
                    if (navController.popBackStack<Screen.CloudSync>(false).not()) {
                        navController.popBackStack()
                        navController.navigate(Screen.CloudSync)
                    }
                },
            )
        }

        composable<Screen.S3Sync> {
            S3SyncRoute(
                goBackToSync = {
                    if (navController.popBackStack<Screen.CloudSync>(false).not()) {
                        navController.popBackStack()
                        navController.navigate(Screen.CloudSync)
                    }
                },
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
                    navController.popBackStack<Screen.QuickSetup>(false)
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

        composable<Screen.Logs> {
            LogsRoute()
        }

        composable<Screen.OpenSourceLibraries> {
            OpenSourceLibrariesRoute()
        }

        composable<Screen.ManageSubscription> {
            ManageSubscriptionRoute()
        }

        composable<Screen.ManageTags> {
            ManageTagsRoute()
        }
    }
}