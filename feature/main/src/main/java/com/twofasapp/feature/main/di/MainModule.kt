/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.main.di

import com.twofasapp.core.di.KoinModule
import com.twofasapp.feature.main.ui.main.MainViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class MainModule : KoinModule {
    override fun provide() = module {
        viewModelOf(::MainViewModel)
    }
}