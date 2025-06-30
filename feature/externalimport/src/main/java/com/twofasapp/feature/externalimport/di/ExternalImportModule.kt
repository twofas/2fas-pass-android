/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.externalimport.di

import com.twofasapp.core.di.KoinModule
import com.twofasapp.feature.externalimport.import.spec.AppleDesktopImportSpec
import com.twofasapp.feature.externalimport.import.spec.AppleMobileImportSpec
import com.twofasapp.feature.externalimport.import.spec.BitwardenImportSpec
import com.twofasapp.feature.externalimport.import.spec.ChromeImportSpec
import com.twofasapp.feature.externalimport.import.spec.DashlaneDesktopImportSpec
import com.twofasapp.feature.externalimport.import.spec.DashlaneMobileImportSpec
import com.twofasapp.feature.externalimport.import.spec.LastPassImportSpec
import com.twofasapp.feature.externalimport.import.spec.OnePasswordImportSpec
import com.twofasapp.feature.externalimport.import.spec.ProtonPassImportSpec
import com.twofasapp.feature.externalimport.ui.externalimport.ExternalImportViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class ExternalImportModule : KoinModule {
    override fun provide() = module {
        viewModelOf(::ExternalImportViewModel)
        singleOf(::BitwardenImportSpec)
        singleOf(::OnePasswordImportSpec)
        singleOf(::ProtonPassImportSpec)
        singleOf(::ChromeImportSpec)
        singleOf(::LastPassImportSpec)
        singleOf(::DashlaneDesktopImportSpec)
        singleOf(::DashlaneMobileImportSpec)
        singleOf(::AppleDesktopImportSpec)
        singleOf(::AppleMobileImportSpec)
    }
}