/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.navigation

import androidx.compose.runtime.Composable
import com.twofasapp.feature.settings.ui.about.AboutScreen
import com.twofasapp.feature.settings.ui.autofill.AutofillScreen
import com.twofasapp.feature.settings.ui.changepassword.current.EnterCurrentPasswordScreen
import com.twofasapp.feature.settings.ui.changepassword.processing.ProcessingNewPasswordScreen
import com.twofasapp.feature.settings.ui.changepassword.set.SetNewPasswordScreen
import com.twofasapp.feature.settings.ui.cloudsync.CloudSyncScreen
import com.twofasapp.feature.settings.ui.customization.CustomizationScreen
import com.twofasapp.feature.settings.ui.importexport.ImportExportScreen
import com.twofasapp.feature.settings.ui.knownbrowsers.KnownBrowsersScreen
import com.twofasapp.feature.settings.ui.lockoutsettings.LockoutSettingsScreen
import com.twofasapp.feature.settings.ui.opensource.OpenSourceLibrariesScreen
import com.twofasapp.feature.settings.ui.pushnotifications.PushNotificationsScreen
import com.twofasapp.feature.settings.ui.savedecryptionkit.SaveDecryptionKitScreen
import com.twofasapp.feature.settings.ui.security.SecurityScreen
import com.twofasapp.feature.settings.ui.securitytier.SecurityTierScreen
import com.twofasapp.feature.settings.ui.settings.SettingsScreen
import com.twofasapp.feature.settings.ui.subscription.ManageSubscriptionScreen
import com.twofasapp.feature.settings.ui.tags.ManageTagsScreen
import com.twofasapp.feature.settings.ui.transfer.TransferScreen
import com.twofasapp.feature.settings.ui.trash.TrashScreen

@Composable
fun SettingsRoute() {
    SettingsScreen()
}

@Composable
fun SecurityRoute() {
    SecurityScreen()
}

@Composable
fun ChangePasswordRoute(
    openSetNewPassword: () -> Unit,
) {
    EnterCurrentPasswordScreen(
        openSetNewPassword = openSetNewPassword,
    )
}

@Composable
fun SetNewPasswordRoute(
    openProcessingNewPassword: (String) -> Unit,
) {
    SetNewPasswordScreen(
        openProcessingNewPassword = openProcessingNewPassword,
    )
}

@Composable
fun ProcessingNewPasswordRoute(
    onOpenDecryptionKit: (String) -> Unit,
    onClose: () -> Unit,
) {
    ProcessingNewPasswordScreen(
        onOpenDecryptionKit = onOpenDecryptionKit,
        onClose = onClose,
    )
}

@Composable
fun LockoutSettingsRoute() {
    LockoutSettingsScreen()
}

@Composable
fun SaveDecryptionKitRoute() {
    SaveDecryptionKitScreen()
}

@Composable
fun ProtectionLevelRoute() {
    SecurityTierScreen()
}

@Composable
fun AutofillRoute() {
    AutofillScreen()
}

@Composable
fun CustomizationRoute() {
    CustomizationScreen()
}

@Composable
fun KnownBrowsersRoute() {
    KnownBrowsersScreen()
}

@Composable
fun PushNotificationsRoute() {
    PushNotificationsScreen()
}

@Composable
fun CloudSyncRoute() {
    CloudSyncScreen()
}

@Composable
fun ImportExportRoute(
    openLogins: () -> Unit,
) {
    ImportExportScreen(
        openLogins = openLogins,
    )
}

@Composable
fun TransferFromOtherAppsRoute() {
    TransferScreen()
}

@Composable
fun TrashRoute() {
    TrashScreen()
}

@Composable
fun AboutRoute() {
    AboutScreen()
}

@Composable
fun OpenSourceLibrariesRoute() {
    OpenSourceLibrariesScreen()
}

@Composable
fun ManageSubscriptionRoute() {
    ManageSubscriptionScreen()
}

@Composable
fun ManageTagsRoute() {
    ManageTagsScreen()
}