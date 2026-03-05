/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.credentialprovider.di

import com.twofasapp.core.di.KoinModule
import com.twofasapp.feature.credentialprovider.handler.PassKeyBeginCreateHandler
import com.twofasapp.feature.credentialprovider.handler.PassKeyBeginGetHandler
import com.twofasapp.feature.credentialprovider.handler.PassKeyCreateHandler
import com.twofasapp.feature.credentialprovider.handler.PassKeyGetHandler
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

class CredentialProviderModule : KoinModule {
    override fun provide() = module {
        singleOf(::PassKeyCreateHandler)
        singleOf(::PassKeyBeginCreateHandler)
        singleOf(::PassKeyBeginGetHandler)
        singleOf(::PassKeyGetHandler)
    }
}