/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.network.cachecontrol.CacheControlCacheStrategy
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import com.pluto.Pluto
import com.pluto.plugins.datastore.pref.PlutoDatastorePreferencesPlugin
import com.pluto.plugins.logger.PlutoLoggerPlugin
import com.pluto.plugins.rooms.db.PlutoRoomsDatabasePlugin
import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.core.common.build.BuildVariant
import com.twofasapp.core.common.services.CrashlyticsInstance
import com.twofasapp.core.common.services.CrashlyticsProvider
import com.twofasapp.core.common.time.TimeProvider
import com.twofasapp.data.purchases.PurchasesRepository
import com.twofasapp.data.settings.SettingsRepository
import com.twofasapp.pass.di.Modules
import com.twofasapp.pass.lifecycle.AppLifecycleObserver
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class App : Application(), SingletonImageLoader.Factory {

    private val appLifecycleObserver: AppLifecycleObserver by inject()
    private val appBuild: AppBuild by inject()
    private val timeProvider: TimeProvider by inject()
    private val crashlyticsProvider: CrashlyticsProvider by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val purchasesRepository: PurchasesRepository by inject()

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(Modules.provide())
        }

        when (appBuild.buildVariant) {
            BuildVariant.Release -> Unit
            BuildVariant.Internal -> Unit
            BuildVariant.Debug -> {
                Timber.plant(Timber.DebugTree())
                System.setProperty("kotlinx.coroutines.debug", "on")
            }
        }

        CrashlyticsInstance.crashlytics = crashlyticsProvider

        purchasesRepository.initialize()

        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)

        GlobalScope.launch {
            crashlyticsProvider.setEnabled(settingsRepository.observeSendCrashLogs().first())

            timeProvider.sync()
        }

        Pluto.Installer(this)
            .apply {
                addPlugin(PlutoRoomsDatabasePlugin())
                addPlugin(PlutoDatastorePreferencesPlugin())

                if (appBuild.buildVariant == BuildVariant.Internal) {
                    addPlugin(PlutoLoggerPlugin())
                }
            }
            .install()
    }

    @OptIn(ExperimentalCoilApi::class)
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())

                add(
                    OkHttpNetworkFetcherFactory(
                        cacheStrategy = {
                            CacheControlCacheStrategy()
                        },
                        callFactory = {
                            OkHttpClient.Builder()
                                .addNetworkInterceptor(IconNotFoundInterceptor())
                                .build()
                        },
                    ),
                )
            }
            .crossfade(true)
            .build()
    }

    private class IconNotFoundInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder().build()
            val response = chain.proceed(request)

            return if (response.code == 404) {
                response.newBuilder().code(200).build()
            } else {
                response
            }
        }
    }
}