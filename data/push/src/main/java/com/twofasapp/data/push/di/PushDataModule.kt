/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.push.di

import android.app.NotificationManager
import android.content.Context
import com.twofasapp.core.di.KoinModule
import com.twofasapp.data.push.PushRepository
import com.twofasapp.data.push.PushRepositoryImpl
import com.twofasapp.data.push.notifications.NotificationSystemChannelProvider
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

class PushDataModule : KoinModule {
    override fun provide(): Module = module {
        factory { androidContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

        singleOf(::PushRepositoryImpl) { bind<PushRepository>() }
        singleOf(::NotificationSystemChannelProvider)
    }
}