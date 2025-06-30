/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.di

import com.twofasapp.core.di.KoinModule
import com.twofasapp.feature.startup.ui.StartupConfig
import com.twofasapp.feature.startup.ui.StartupViewModel
import com.twofasapp.feature.startup.ui.createdecryptionkit.CreateDecryptionKitViewModel
import com.twofasapp.feature.startup.ui.createmasterpassword.CreateMasterPasswordViewModel
import com.twofasapp.feature.startup.ui.createsecretkey.create.CreateSecretKeyViewModel
import com.twofasapp.feature.startup.ui.createsecretkey.success.CreateSecretKeySuccessViewModel
import com.twofasapp.feature.startup.ui.restorevault.RestoreState
import com.twofasapp.feature.startup.ui.restorevault.RestoreVaultViewModel
import com.twofasapp.feature.startup.ui.restorevault.cloudfiles.CloudFilesViewModel
import com.twofasapp.feature.startup.ui.restorevault.decyptvault.DecryptVaultViewModel
import com.twofasapp.feature.startup.ui.restorevault.webdav.WebDavRestoreViewModel
import com.twofasapp.feature.startup.ui.vaultsetup.completed.VaultSetupCompletedViewModel
import com.twofasapp.feature.startup.ui.vaultsetup.start.VaultSetupStartViewModel
import com.twofasapp.feature.startup.ui.welcome.WelcomeViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class StartupModule : KoinModule {
    override fun provide() = module {
        viewModelOf(::StartupViewModel)
        viewModelOf(::WelcomeViewModel)
        viewModelOf(::VaultSetupStartViewModel)
        viewModelOf(::VaultSetupCompletedViewModel)
        viewModelOf(::CreateSecretKeyViewModel)
        viewModelOf(::CreateSecretKeySuccessViewModel)
        viewModelOf(::RestoreVaultViewModel)
        viewModelOf(::CreateMasterPasswordViewModel)
        viewModelOf(::CreateDecryptionKitViewModel)
        viewModelOf(::WebDavRestoreViewModel)
        viewModelOf(::CloudFilesViewModel)
        viewModelOf(::DecryptVaultViewModel)

        singleOf(::StartupConfig)
        singleOf(::RestoreState)
    }
}