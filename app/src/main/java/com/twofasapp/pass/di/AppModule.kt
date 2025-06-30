/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.di

import com.instacart.truetime.time.TrueTime
import com.instacart.truetime.time.TrueTimeImpl
import com.twofasapp.core.android.deeplinks.Deeplinks
import com.twofasapp.core.common.auth.AuthStatusTracker
import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.core.common.build.Device
import com.twofasapp.core.common.build.LocalConfig
import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.push.PushTokenProvider
import com.twofasapp.core.common.services.CrashlyticsProvider
import com.twofasapp.core.common.time.TimeProvider
import com.twofasapp.core.di.KoinModule
import com.twofasapp.core.locale.Strings
import com.twofasapp.data.push.notifications.NotificationsHandler
import com.twofasapp.feature.settings.ui.opensource.OpenSourceLibrariesProvider
import com.twofasapp.pass.auth.AuthStatusTrackerImpl
import com.twofasapp.pass.build.AppBuildImpl
import com.twofasapp.pass.build.DeviceImpl
import com.twofasapp.pass.build.LocalConfigImpl
import com.twofasapp.pass.coroutines.AppDispatchers
import com.twofasapp.pass.deeplinks.DeeplinksHandler
import com.twofasapp.pass.lifecycle.AppLifecycleObserver
import com.twofasapp.pass.notifications.SystemNotificationsHandler
import com.twofasapp.pass.notifications.browserrequest.BrowserRequestNotification
import com.twofasapp.pass.oss.OpenSourceLibrariesProviderImpl
import com.twofasapp.pass.push.FcmTokenProvider
import com.twofasapp.pass.servcies.CrashlyticsProviderFirebase
import com.twofasapp.pass.time.TimeProviderImpl
import com.twofasapp.pass.ui.app.AppViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class AppModule : KoinModule {

    override fun provide(): Module = module {
        singleOf(::Strings)
        singleOf(::AppDispatchers) { bind<Dispatchers>() }
        singleOf(::AppBuildImpl) { bind<AppBuild>() }
        singleOf(::LocalConfigImpl) { bind<LocalConfig>() }
        singleOf(::AuthStatusTrackerImpl) { bind<AuthStatusTracker>() }
        singleOf(::AppLifecycleObserver)

        single<TrueTime> { TrueTimeImpl() }
        singleOf(::TimeProviderImpl) { bind<TimeProvider>() }

        singleOf(::OpenSourceLibrariesProviderImpl) { bind<OpenSourceLibrariesProvider>() }

        viewModelOf(::AppViewModel)

        singleOf(::DeeplinksHandler) { bind<Deeplinks>() }
        singleOf(::FcmTokenProvider) { bind<PushTokenProvider>() }
        singleOf(::SystemNotificationsHandler) { bind<NotificationsHandler>() }
        singleOf(::CrashlyticsProviderFirebase) { bind<CrashlyticsProvider>() }
        singleOf(::DeviceImpl) { bind<Device>() }
        singleOf(::BrowserRequestNotification)
    }
}