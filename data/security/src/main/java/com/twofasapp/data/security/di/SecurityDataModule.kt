/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.security.di

import com.twofasapp.core.di.KoinModule
import com.twofasapp.data.security.crypto.KdfCalculator
import com.twofasapp.data.security.crypto.KdfCalculatorImpl
import com.twofasapp.data.security.crypto.MasterKeyGenerator
import com.twofasapp.data.security.crypto.SeedGenerator
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

class SecurityDataModule : KoinModule {
    override fun provide(): Module = module {
        singleOf(::SeedGenerator)
        singleOf(::KdfCalculatorImpl) { bind<KdfCalculator>() }
        singleOf(::MasterKeyGenerator)
    }
}