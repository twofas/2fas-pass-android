/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.di

import com.twofasapp.core.crypto.di.CryptoModule
import com.twofasapp.core.design.feature.tags.ManageTagModule
import com.twofasapp.core.network.di.NetworkModule
import com.twofasapp.data.cloud.di.CloudDataModule
import com.twofasapp.data.main.di.MainDataModule
import com.twofasapp.data.purchases.di.PurchasesDataModule
import com.twofasapp.data.push.di.PushDataModule
import com.twofasapp.data.security.di.SecurityDataModule
import com.twofasapp.data.settings.di.SettingsDataModule
import com.twofasapp.feature.autofill.di.AutofillModule
import com.twofasapp.feature.cloudsync.di.CloudSyncModule
import com.twofasapp.feature.connect.di.ConnectModule
import com.twofasapp.feature.developer.di.DeveloperModule
import com.twofasapp.feature.di.QrScanModule
import com.twofasapp.feature.externalimport.di.ExternalImportModule
import com.twofasapp.feature.home.di.HomeModule
import com.twofasapp.feature.itemform.di.ItemFormModule
import com.twofasapp.feature.lock.di.LockModule
import com.twofasapp.feature.main.di.MainModule
import com.twofasapp.feature.purchases.di.PurchasesModule
import com.twofasapp.feature.quicksetup.di.QuickSetupModule
import com.twofasapp.feature.settings.di.SettingsModule
import com.twofasapp.feature.startup.di.StartupModule
import org.koin.core.module.Module

object Modules {
    private val app = listOf(
        AppModule(),
        StorageModule(),
        NetworkModule(),
        CryptoModule(),
    )

    private val data = listOf(
        MainDataModule(),
        SettingsDataModule(),
        SecurityDataModule(),
        PushDataModule(),
        CloudDataModule(),
        PurchasesDataModule(),
    )

    private val feature = listOf(
        DeveloperModule(),
        StartupModule(),
        MainModule(),
        LockModule(),
        AutofillModule(),
        HomeModule(),
        ConnectModule(),
        SettingsModule(),
        ExternalImportModule(),
        QrScanModule(),
        CloudSyncModule(),
        ItemFormModule(),
        PurchasesModule(),
        QuickSetupModule(),
        ManageTagModule()
    )

    fun provide(): List<Module> =
        buildList {
            addAll(app)
            addAll(data)
            addAll(feature)
        }.map { it.provide() }
}