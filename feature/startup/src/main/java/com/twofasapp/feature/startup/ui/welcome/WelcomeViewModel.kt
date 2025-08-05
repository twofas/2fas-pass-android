/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.welcome

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.auth.AuthStatusTracker
import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.core.common.build.BuildVariant
import com.twofasapp.core.common.domain.crypto.KdfSpec
import com.twofasapp.data.main.SecurityRepository
import com.twofasapp.data.settings.SessionRepository
import com.twofasapp.feature.startup.ui.StartupConfig
import kotlinx.coroutines.flow.MutableStateFlow

internal class WelcomeViewModel(
    appBuild: AppBuild,
    private val sessionRepository: SessionRepository,
    private val securityRepository: SecurityRepository,
    private val startupConfig: StartupConfig,
    private val authStatusTracker: AuthStatusTracker,
) : ViewModel() {
    val uiState = MutableStateFlow(
        WelcomeUiState(
            debuggable = when (appBuild.buildVariant) {
                BuildVariant.Release -> false
                BuildVariant.Internal -> false
                BuildVariant.Debug -> true
            },
        ),
    )

    fun devSkip() {
        launchScoped {
            startupConfig.seed = securityRepository.generateSeed()

            val masterKey = securityRepository.generateMasterKeyOnFirstLaunch(
                password = "pass12345",
                seed = startupConfig.seed!!,
                kdfSpec = KdfSpec.Argon2id(),
            )

            startupConfig.masterKey = masterKey

            startupConfig.finishStartup()
            authStatusTracker.authenticate()
            sessionRepository.setQuickSetupPrompted(true)
            sessionRepository.setStartupCompleted(true)
        }
    }
}