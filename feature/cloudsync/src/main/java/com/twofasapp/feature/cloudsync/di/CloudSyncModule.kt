/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.cloudsync.di

import com.twofasapp.core.di.KoinModule
import com.twofasapp.feature.cloudsync.ui.common.SyncStatusViewModel
import com.twofasapp.feature.cloudsync.ui.googledrive.GoogleDriveSyncViewModel
import com.twofasapp.feature.cloudsync.ui.webdav.WebDavSyncViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class CloudSyncModule : KoinModule {
    override fun provide() = module {
        viewModelOf(::SyncStatusViewModel)
        viewModelOf(::GoogleDriveSyncViewModel)
        viewModelOf(::WebDavSyncViewModel)
    }
}