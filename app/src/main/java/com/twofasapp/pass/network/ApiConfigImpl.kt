/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.network

import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.core.common.build.BuildVariant
import com.twofasapp.core.network.ApiConfig
import com.twofasapp.core.network.ApiEnvironment
import com.twofasapp.data.settings.DeveloperRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

internal class ApiConfigImpl(
    private val appBuild: AppBuild,
    private val developerRepository: DeveloperRepository,
) : ApiConfig {

    private val environment: ApiEnvironment by lazy {
        if (appBuild.buildVariant == BuildVariant.Release) {
            ApiEnvironment.Production
        } else {
            runBlocking { developerRepository.observeApiEnvironment().first() }
        }
    }

    override val apiUrl: String by lazy {
        when (environment) {
            ApiEnvironment.Production -> ApiConfig.ProductionApiUrl
            ApiEnvironment.Dev -> ApiConfig.DevApiUrl
        }
    }

    override val wssUrl: String by lazy {
        when (environment) {
            ApiEnvironment.Production -> ApiConfig.ProductionWssUrl
            ApiEnvironment.Dev -> ApiConfig.DevWssUrl
        }
    }

    override val shareApiUrl: String by lazy {
        when (environment) {
            ApiEnvironment.Production -> ApiConfig.ProductionShareApiUrl
            ApiEnvironment.Dev -> ApiConfig.DevShareApiUrl
        }
    }
}