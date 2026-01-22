/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.di

import com.twofasapp.core.di.KoinModule
import com.twofasapp.feature.settings.ui.about.AboutViewModel
import com.twofasapp.feature.settings.ui.autofill.AutofillViewModel
import com.twofasapp.feature.settings.ui.autofill.browsers.BrowserAutofillManager
import com.twofasapp.feature.settings.ui.autofill.browsers.BrowserAutofillManagerImpl
import com.twofasapp.feature.settings.ui.backupdecryption.BackupDecryptionViewModel
import com.twofasapp.feature.settings.ui.changepassword.current.EnterCurrentPasswordViewModel
import com.twofasapp.feature.settings.ui.changepassword.processing.ProcessingNewPasswordViewModel
import com.twofasapp.feature.settings.ui.changepassword.set.SetNewPasswordViewModel
import com.twofasapp.feature.settings.ui.cloudsync.CloudSyncViewModel
import com.twofasapp.feature.settings.ui.customization.CustomizationViewModel
import com.twofasapp.feature.settings.ui.importexport.ImportExportViewModel
import com.twofasapp.feature.settings.ui.knownbrowsers.KnownBrowsersViewModel
import com.twofasapp.feature.settings.ui.lockoutsettings.LockoutSettingsViewModel
import com.twofasapp.feature.settings.ui.pushnotifications.PushNotificationsViewModel
import com.twofasapp.feature.settings.ui.savedecryptionkit.SaveDecryptionKitViewModel
import com.twofasapp.feature.settings.ui.security.SecurityViewModel
import com.twofasapp.feature.settings.ui.securitytier.SecurityTierViewModel
import com.twofasapp.feature.settings.ui.settings.SettingsViewModel
import com.twofasapp.feature.settings.ui.subscription.ManageSubscriptionViewModel
import com.twofasapp.feature.settings.ui.tags.ManageTagsViewModel
import com.twofasapp.feature.settings.ui.transfer.TransferViewModel
import com.twofasapp.feature.settings.ui.trash.TrashViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class SettingsModule : KoinModule {
    override fun provide() = module {
        singleOf(::BrowserAutofillManagerImpl) { bind<BrowserAutofillManager>() }

        viewModelOf(::SettingsViewModel)
        viewModelOf(::CustomizationViewModel)
        viewModelOf(::AutofillViewModel)
        viewModelOf(::AboutViewModel)
        viewModelOf(::CloudSyncViewModel)
        viewModelOf(::ImportExportViewModel)
        viewModelOf(::TransferViewModel)
        viewModelOf(::SecurityViewModel)
        viewModelOf(::TrashViewModel)
        viewModelOf(::BackupDecryptionViewModel)
        viewModelOf(::EnterCurrentPasswordViewModel)
        viewModelOf(::SetNewPasswordViewModel)
        viewModelOf(::ProcessingNewPasswordViewModel)
        viewModelOf(::KnownBrowsersViewModel)
        viewModelOf(::PushNotificationsViewModel)
        viewModelOf(::LockoutSettingsViewModel)
        viewModelOf(::SecurityTierViewModel)
        viewModelOf(::SaveDecryptionKitViewModel)
        viewModelOf(::ManageSubscriptionViewModel)
        viewModelOf(::ManageTagsViewModel)
    }
}