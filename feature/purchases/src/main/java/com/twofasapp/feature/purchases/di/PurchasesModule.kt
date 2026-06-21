/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.purchases.di

import com.twofasapp.core.di.KoinModule
import com.twofasapp.feature.purchases.appreview.AppReviewViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class PurchasesModule : KoinModule {

    override fun provide() = module {
        viewModelOf(::AppReviewViewModel)
    }
}