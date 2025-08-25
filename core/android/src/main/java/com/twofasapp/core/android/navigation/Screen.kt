/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.android.navigation

import com.twofasapp.core.common.domain.ImportType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Screen(
    @SerialName(NavArgKey.ScreenType) val screenType: ScreenType = ScreenType.Standard,
) {
    val route: String
        get() = javaClass.kotlin.qualifiedName.orEmpty()

    @Serializable
    data object StartupContainer : Screen()

    @Serializable
    data object MainContainer : Screen()

    @Serializable
    data object Welcome : Screen()

    @Serializable
    data object VaultSetupStart : Screen()

    @Serializable
    data object VaultSetupHalfWay : Screen()

    @Serializable
    data object VaultSetupCompleted : Screen()

    @Serializable
    data object CreateSecretKey : Screen()

    @Serializable
    data object CreateSecretKeySuccess : Screen()

    @Serializable
    data object CreateMasterPassword : Screen()

    @Serializable
    data object CreateDecryptionKit : Screen()

    @Serializable
    data object RestoreVault : Screen()

    @Serializable
    data object RestoreWebDav : Screen()

    @Serializable
    data object RestoreCloudFiles : Screen()

    @Serializable
    data object DecryptVault : Screen()

    @Serializable
    data object AutofillPicker : Screen()

    @Serializable
    class Home : Screen(screenType = ScreenType.TopLevel)

    @Serializable
    class Connect : Screen(screenType = ScreenType.TopLevel)

    @Serializable
    class Settings : Screen(screenType = ScreenType.TopLevel)

    @Serializable
    data object QuickSetup : Screen()

    @Serializable
    data object Developer : Screen()

    @Serializable
    data class EditLogin(val vaultId: String, val loginId: String) : Screen()

    @Serializable
    data object Security : Screen()

    @Serializable
    data object ChangePassword : Screen()

    @Serializable
    data object SetNewPassword : Screen()

    @Serializable
    data class ProcessingNewPassword(val encryptedPassword: String) : Screen()

    @Serializable
    data object LockoutSettings : Screen()

    @Serializable
    data class SaveDecryptionKit(val masterKeyHex: String) : Screen()

    @Serializable
    data object SecurityType : Screen()

    @Serializable
    data object Autofill : Screen()

    @Serializable
    data object Customization : Screen()

    @Serializable
    data object ManageTags : Screen()

    @Serializable
    data object KnownBrowsers : Screen()

    @Serializable
    data object PushNotifications : Screen()

    @Serializable
    data object CloudSync : Screen()

    @Serializable
    data class GoogleDriveSync(
        val openedFromQuickSetup: Boolean,
        val startAuth: Boolean,
    ) : Screen()

    @Serializable
    data object WebDavSync : Screen()

    @Serializable
    data object ImportExport : Screen()

    @Serializable
    data class ExternalImport(val importType: ImportType) : Screen()

    @Serializable
    data object TransferFromOtherApps : Screen()

    @Serializable
    data object Trash : Screen()

    @Serializable
    data object About : Screen()

    @Serializable
    data object OpenSourceLibraries : Screen()

    @Serializable
    data object ManageSubscription : Screen()
}