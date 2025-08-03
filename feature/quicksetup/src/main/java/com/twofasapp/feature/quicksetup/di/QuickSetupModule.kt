package com.twofasapp.feature.quicksetup.di

import com.twofasapp.core.di.KoinModule
import com.twofasapp.feature.quicksetup.ui.QuickSetupViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class QuickSetupModule : KoinModule {
    override fun provide() = module {
        viewModelOf(::QuickSetupViewModel)
    }
}