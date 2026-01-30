/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.externalimport.ui.externalimport

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.android.ktx.runSafely
import com.twofasapp.core.android.navigation.Screen
import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.domain.ImportType
import com.twofasapp.data.main.ItemsRepository
import com.twofasapp.data.main.TagsRepository
import com.twofasapp.feature.externalimport.import.ImportContent
import com.twofasapp.feature.externalimport.import.spec.AppleDesktopImportSpec
import com.twofasapp.feature.externalimport.import.spec.AppleMobileImportSpec
import com.twofasapp.feature.externalimport.import.spec.BitwardenImportSpec
import com.twofasapp.feature.externalimport.import.spec.ChromeImportSpec
import com.twofasapp.feature.externalimport.import.spec.DashlaneDesktopImportSpec
import com.twofasapp.feature.externalimport.import.spec.DashlaneMobileImportSpec
import com.twofasapp.feature.externalimport.import.spec.EnpassImportSpec
import com.twofasapp.feature.externalimport.import.spec.FirefoxImportSpec
import com.twofasapp.feature.externalimport.import.spec.KeepassImportSpec
import com.twofasapp.feature.externalimport.import.spec.KeepassXcImportSpec
import com.twofasapp.feature.externalimport.import.spec.KeeperImportSpec
import com.twofasapp.feature.externalimport.import.spec.LastPassImportSpec
import com.twofasapp.feature.externalimport.import.spec.MicrosoftEdgeImportSpec
import com.twofasapp.feature.externalimport.import.spec.NordPassImportSpec
import com.twofasapp.feature.externalimport.import.spec.OnePasswordImportSpec
import com.twofasapp.feature.externalimport.import.spec.ProtonPassImportSpec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

internal class ExternalImportViewModel(
    savedStateHandle: SavedStateHandle,
    private val dispatchers: Dispatchers,
    private val itemsRepository: ItemsRepository,
    private val tagsRepository: TagsRepository,
    private val bitwardenImportSpec: BitwardenImportSpec,
    private val onePasswordImportSpec: OnePasswordImportSpec,
    private val protonPassImportSpec: ProtonPassImportSpec,
    private val chromeImportSpec: ChromeImportSpec,
    private val microsoftEdgeImportSpec: MicrosoftEdgeImportSpec,
    private val enpassImportSpec: EnpassImportSpec,
    private val lastPassImportSpec: LastPassImportSpec,
    private val dashlaneDesktopImportSpec: DashlaneDesktopImportSpec,
    private val dashlaneMobileImportSpec: DashlaneMobileImportSpec,
    private val appleDesktopImportSpec: AppleDesktopImportSpec,
    private val appleMobileImportSpec: AppleMobileImportSpec,
    private val firefoxImportSpec: FirefoxImportSpec,
    private val keepassImportSpec: KeepassImportSpec,
    private val keepassXcImportSpec: KeepassXcImportSpec,
    private val keeperImportSpec: KeeperImportSpec,
    private val nordPassImportSpec: NordPassImportSpec
) : ViewModel() {
    val uiState = MutableStateFlow(ExternalImportUiState())
    private val importType = savedStateHandle.toRoute<Screen.ExternalImport>().importType

    init {
        uiState.update { state ->
            state.copy(
                importSpec = when (importType) {
                    ImportType.Bitwarden -> bitwardenImportSpec
                    ImportType.OnePassword -> onePasswordImportSpec
                    ImportType.ProtonPass -> protonPassImportSpec
                    ImportType.Chrome -> chromeImportSpec
                    ImportType.MicrosoftEdge -> microsoftEdgeImportSpec
                    ImportType.Enpass -> enpassImportSpec
                    ImportType.LastPass -> lastPassImportSpec
                    ImportType.DashlaneDesktop -> dashlaneDesktopImportSpec
                    ImportType.DashlaneMobile -> dashlaneMobileImportSpec
                    ImportType.AppleDesktop -> appleDesktopImportSpec
                    ImportType.AppleMobile -> appleMobileImportSpec
                    ImportType.Firefox -> firefoxImportSpec
                    ImportType.KeePass -> keepassImportSpec
                    ImportType.KeePassXC -> keepassXcImportSpec
                    ImportType.Keeper -> keeperImportSpec
                    ImportType.NordPass -> nordPassImportSpec
                },
            )
        }
    }

    fun readContent(uri: Uri) {
        uiState.update { it.copy(loading = true) }

        launchScoped(dispatchers.io) {
            runSafely { uiState.value.importSpec.readContent(uri) }
                .onSuccess { updateState(ImportState.ReadSuccess(it)) }
                .onFailure {
                    Timber.e(it)
                    updateState(ImportState.Error(it.message))
                }
        }
    }

    fun startImport(importContent: ImportContent, onSuccess: () -> Unit) {
        uiState.update { it.copy(loading = true) }

        launchScoped {
            runSafely {
                itemsRepository.importItems(importContent.items)
                tagsRepository.importTags(importContent.tags)
            }
                .onSuccess { onSuccess() }
                .onFailure { updateState(ImportState.Error(it.message)) }
        }
    }

    fun tryAgain() {
        updateState(ImportState.Default)
    }

    private fun updateState(importState: ImportState) {
        uiState.update { it.copy(importState = importState, loading = false) }
    }
}