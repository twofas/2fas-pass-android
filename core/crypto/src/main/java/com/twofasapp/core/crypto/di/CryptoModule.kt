/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.crypto.di

import com.twofasapp.core.common.crypto.AndroidKeyStore
import com.twofasapp.core.crypto.AndroidKeyStoreImpl
import com.twofasapp.core.di.KoinModule
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

class CryptoModule : KoinModule {

    override fun provide() = module {
        singleOf(::AndroidKeyStoreImpl) { bind<AndroidKeyStore>() }
    }
}