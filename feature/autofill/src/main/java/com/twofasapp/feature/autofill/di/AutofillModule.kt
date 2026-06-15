/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.di

import com.twofasapp.core.di.KoinModule
import com.twofasapp.feature.autofill.service.handlers.FillRequestHandler
import com.twofasapp.feature.autofill.service.handlers.SaveRequestHandler
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

class AutofillModule : KoinModule {
    override fun provide() = module {
        singleOf(::FillRequestHandler)
        singleOf(::SaveRequestHandler)
    }
}