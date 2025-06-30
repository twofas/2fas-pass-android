/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.navigation

import androidx.compose.runtime.Composable
import com.twofasapp.feature.startup.ui.StartupContainer
import com.twofasapp.feature.startup.ui.createdecryptionkit.CreateDecryptionKitScreen
import com.twofasapp.feature.startup.ui.createmasterpassword.CreateMasterPasswordScreen
import com.twofasapp.feature.startup.ui.createsecretkey.create.CreateSecretKeyScreen
import com.twofasapp.feature.startup.ui.createsecretkey.success.CreateSecretKeySuccessScreen
import com.twofasapp.feature.startup.ui.restorevault.RestoreVaultScreen
import com.twofasapp.feature.startup.ui.restorevault.cloudfiles.CloudFilesScreen
import com.twofasapp.feature.startup.ui.restorevault.decyptvault.DecryptVaultScreen
import com.twofasapp.feature.startup.ui.restorevault.webdav.WebDavRestoreScreen
import com.twofasapp.feature.startup.ui.vaultsetup.completed.VaultSetupCompletedScreen
import com.twofasapp.feature.startup.ui.vaultsetup.halfway.VaultSetupHalfWayScreen
import com.twofasapp.feature.startup.ui.vaultsetup.start.VaultSetupStartScreen
import com.twofasapp.feature.startup.ui.welcome.WelcomeScreen

@Composable
fun StartupRoute() {
    StartupContainer()
}

@Composable
internal fun WelcomeRoute(
    openStartVault: () -> Unit,
    openRestoreVault: () -> Unit,
) {
    WelcomeScreen(
        openStartVault = openStartVault,
        openRestoreVault = openRestoreVault,
    )
}

@Composable
internal fun VaultSetupStartRoute(
    openCreateVault: () -> Unit,
) {
    VaultSetupStartScreen(
        openCreateVault = openCreateVault,
    )
}

@Composable
internal fun VaultSetupHalfWayRoute(
    openCreateMasterPassword: () -> Unit,
) {
    VaultSetupHalfWayScreen(
        openCreateMasterPassword = openCreateMasterPassword,
    )
}

@Composable
internal fun VaultSetupCompletedRoute() {
    VaultSetupCompletedScreen()
}

@Composable
internal fun CreateSecretKeyRoute(
    openSecretKeySuccess: () -> Unit,
) {
    CreateSecretKeyScreen(
        openSecretKeySuccess = openSecretKeySuccess,
    )
}

@Composable
internal fun CreateSecretKeySuccessRoute(
    openCreateMasterPassword: () -> Unit,
) {
    CreateSecretKeySuccessScreen(
        openCreateMasterPassword = openCreateMasterPassword,
    )
}

@Composable
internal fun CreateMasterPasswordRoute(
    openNextStep: () -> Unit,
) {
    CreateMasterPasswordScreen(
        openNextStep = openNextStep,
    )
}

@Composable
internal fun CreateDecryptionKitRoute(
    openNextStep: () -> Unit,
) {
    CreateDecryptionKitScreen(
        openNextStep = openNextStep,
    )
}

@Composable
internal fun RestoreVaultRoute(
    openRestoreCloudFiles: () -> Unit,
    openRestoreWebDavConfig: () -> Unit,
    openDecryptVault: () -> Unit,
) {
    RestoreVaultScreen(
        openRestoreCloudFiles = openRestoreCloudFiles,
        openRestoreWebDavConfig = openRestoreWebDavConfig,
        openDecryptVault = openDecryptVault,
    )
}

@Composable
internal fun RestoreWebDavRoute(
    openRestoreCloudFiles: () -> Unit,
) {
    WebDavRestoreScreen(
        openRestoreCloudFiles = openRestoreCloudFiles,
    )
}

@Composable
internal fun RestoreCloudFilesRoute(
    openDecryptVault: () -> Unit,
) {
    CloudFilesScreen(
        openDecryptVault = openDecryptVault,
    )
}

@Composable
internal fun DecryptVaultRoute() {
    DecryptVaultScreen()
}