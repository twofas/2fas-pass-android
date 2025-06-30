/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.about

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.core.common.build.BuildVariant
import com.twofasapp.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class AboutViewModel(
    private val settingsRepository: SettingsRepository,
    private val appBuild: AppBuild,
) : ViewModel() {
    val uiState = MutableStateFlow(AboutUiState())

    init {
        launchScoped {
            uiState.update {
                it.copy(
                    version = buildString {
                        append("${appBuild.versionName} (${appBuild.versionCode})")

                        when (appBuild.buildVariant) {
                            BuildVariant.Release -> Unit
                            BuildVariant.Internal -> append(" - internal")
                            BuildVariant.Debug -> append(" - debug")
                        }
                    },
                )
            }
        }

        launchScoped {
            settingsRepository.observeSendCrashLogs().collect { sendCrashLogs ->
                uiState.update { it.copy(crashLogsEnabled = sendCrashLogs) }
            }
        }
    }

    fun toggleCrashLogs() {
        launchScoped {
            settingsRepository.setSendCrashLogs(uiState.value.crashLogsEnabled.not())
        }
    }
}