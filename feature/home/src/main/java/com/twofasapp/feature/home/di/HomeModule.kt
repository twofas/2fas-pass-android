/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.di

import com.twofasapp.core.di.KoinModule
import com.twofasapp.feature.autofill.service.builders.AutofillActivityIntents
import com.twofasapp.feature.home.ui.autofill.AutofillActivityIntentsImpl
import com.twofasapp.feature.home.ui.autofill.AutofillViewModel
import com.twofasapp.feature.home.ui.autofill.auth.AutofillAuthViewModel
import com.twofasapp.feature.home.ui.autofill.picker.AutofillPickerViewModel
import com.twofasapp.feature.home.ui.autofill.save.AutofillSaveLoginViewModel
import com.twofasapp.feature.home.ui.editItem.EditItemViewModel
import com.twofasapp.feature.home.ui.home.HomeViewModel
import com.twofasapp.feature.home.ui.itemdetails.ItemDetailsViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class HomeModule : KoinModule {
    override fun provide() = module {
        viewModelOf(::HomeViewModel)
        viewModelOf(::EditItemViewModel)
        viewModelOf(::ItemDetailsViewModel)
        viewModelOf(::AutofillViewModel)
        viewModelOf(::AutofillPickerViewModel)
        viewModelOf(::AutofillAuthViewModel)
        viewModelOf(::AutofillSaveLoginViewModel)

        singleOf(::AutofillActivityIntentsImpl) { bind<AutofillActivityIntents>() }
    }
}