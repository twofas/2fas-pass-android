/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.di

import com.twofasapp.core.di.KoinModule
import com.twofasapp.data.cloud.services.CloudServiceProvider
import com.twofasapp.data.cloud.services.CloudServiceProviderImpl
import com.twofasapp.data.cloud.services.googledrive.GoogleDriveCloudService
import com.twofasapp.data.cloud.services.webdav.WebDavClient
import com.twofasapp.data.cloud.services.webdav.WebDavCloudService
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

class CloudDataModule : KoinModule {
    override fun provide(): Module = module {
        singleOf(::CloudServiceProviderImpl) { bind<CloudServiceProvider>() }
        singleOf(::GoogleDriveCloudService)
        singleOf(::WebDavCloudService)
        singleOf(::WebDavClient)
    }
}