/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.autofill

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class AutofillViewModel(
    appBuild: AppBuild,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val uiState = MutableStateFlow(
        AutofillUiState(
            packageName = appBuild.packageName,
        ),
    )

    init {
        launchScoped {
            settingsRepository.observeAutofillSettings().collect { settings ->
                uiState.update { it.copy(autofillInline = settings.useInlinePresentation) }
            }
        }
    }

    fun updateAutofillInline() {
        launchScoped {
            settingsRepository.setAutofillSettings(useInline = uiState.value.autofillInline.not())
        }
    }
}