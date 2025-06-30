/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.purchases.di

import com.twofasapp.core.di.KoinModule
import com.twofasapp.data.purchases.PurchasesOverrideRepository
import com.twofasapp.data.purchases.PurchasesOverrideRepositoryImpl
import com.twofasapp.data.purchases.PurchasesRepository
import com.twofasapp.data.purchases.PurchasesRepositoryImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

class PurchasesDataModule : KoinModule {
    override fun provide(): Module = module {
        singleOf(::PurchasesRepositoryImpl) { bind<PurchasesRepository>() }
        singleOf(::PurchasesOverrideRepositoryImpl) { bind<PurchasesOverrideRepository>() }
    }
}