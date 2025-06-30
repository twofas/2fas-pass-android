/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.settings.di

import com.twofasapp.core.di.KoinModule
import com.twofasapp.data.settings.SessionRepository
import com.twofasapp.data.settings.SessionRepositoryImpl
import com.twofasapp.data.settings.SettingsRepository
import com.twofasapp.data.settings.SettingsRepositoryImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

class SettingsDataModule : KoinModule {
    override fun provide(): Module = module {
        singleOf(::SessionRepositoryImpl) { bind<SessionRepository>() }
        singleOf(::SettingsRepositoryImpl) { bind<SettingsRepository>() }
    }
}