/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.connect.di

import com.twofasapp.core.di.KoinModule
import com.twofasapp.feature.connect.ui.connect.ConnectViewModel
import com.twofasapp.feature.connect.ui.connectmodal.ConnectModalViewModel
import com.twofasapp.feature.connect.ui.requestmodal.RequestModalViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class ConnectModule : KoinModule {
    override fun provide() = module {
        viewModelOf(::ConnectViewModel)
        viewModelOf(::ConnectModalViewModel)
        viewModelOf(::RequestModalViewModel)
    }
}