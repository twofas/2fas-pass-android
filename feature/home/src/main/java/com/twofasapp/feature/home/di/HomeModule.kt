/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.di

import com.twofasapp.core.di.KoinModule
import com.twofasapp.feature.home.ui.editItem.EditItemViewModel
import com.twofasapp.feature.home.ui.home.HomeViewModel
import com.twofasapp.feature.home.ui.home.modal.itemdetails.ItemDetailsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class HomeModule : KoinModule {
    override fun provide() = module {
        viewModelOf(::HomeViewModel)
        viewModelOf(::EditItemViewModel)
        viewModelOf(::ItemDetailsViewModel)
    }
}