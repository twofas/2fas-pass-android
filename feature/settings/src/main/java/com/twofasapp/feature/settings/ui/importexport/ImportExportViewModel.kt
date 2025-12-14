/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.importexport

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.android.ktx.runSafely
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.time.TimeProvider
import com.twofasapp.data.main.BackupRepository
import com.twofasapp.data.main.ItemsRepository
import com.twofasapp.data.main.TagsRepository
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.main.domain.InvalidSchemaVersionException
import com.twofasapp.data.purchases.PurchasesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class ImportExportViewModel(
    private val itemsRepository: ItemsRepository,
    private val vaultsRepository: VaultsRepository,
    private val backupRepository: BackupRepository,
    private val purchasesRepository: PurchasesRepository,
    private val tagsRepository: TagsRepository,
    private val timeProvider: TimeProvider,
) : ViewModel() {
    val uiState = MutableStateFlow(ImportExportUiState())
    private var importJob: Job? = null

    init {
        launchScoped {
            purchasesRepository.observeSubscriptionPlan().collect { plan ->
                launchScoped {
                    uiState.update { state ->
                        state.copy(
                            maxItems = plan.entitlements.itemsLimit,
                            isItemsLimitReached = itemsRepository.getItemsCount() >= plan.entitlements.itemsLimit,
                        )
                    }
                }
            }
        }
    }

    fun generateBackup(encrypted: Boolean, onComplete: (String, String) -> Unit) {
        launchScoped {
            val vaultBackup = if (encrypted) {
                backupRepository.encryptVaultBackup(
                    backupRepository.createVaultBackup(
                        vaultId = vaultsRepository.getVault().id,
                        includeDeleted = false,
                        decryptSecretFields = false,
                    ),
                )
            } else {
                backupRepository.createVaultBackup(
                    vaultId = vaultsRepository.getVault().id,
                    includeDeleted = false,
                    decryptSecretFields = true,
                )
            }

            val backup = backupRepository.serializeVaultBackup(
                vaultBackup = vaultBackup,
            )

            publishEvent(ImportExportUiEvent.ResetExportModal)
            onComplete(vaultBackup.generateFilename(), backup)
        }
    }

    fun readBackup(fileUri: Uri) {
        showImportLoading(true)

        launchScoped {
            runSafely { backupRepository.readVaultBackup(fileUri) }
                .onSuccess { backup ->
                    uiState.update { it.copy(vaultBackupToImport = backup) }
                    tryToImport()
                }
                .onFailure {
                    showImportLoading(false)

                    if (it is InvalidSchemaVersionException) {
                        uiState.update { state ->
                            state.copy(
                                vaultBackupToImport = state.vaultBackupToImport.copy(schemaVersion = it.backupSchemaVersion),
                            )
                        }
                        publishEvent(ImportExportUiEvent.ShowInvalidSchemaError)
                    } else {
                        publishEvent(ImportExportUiEvent.ShowErrorDialog)
                    }
                }
        }
    }

    fun consumeEvent(event: ImportExportUiEvent) {
        uiState.update { it.copy(events = it.events.minus(event)) }
    }

    private fun publishEvent(event: ImportExportUiEvent) {
        uiState.update { it.copy(events = it.events.plus(event)) }
    }

    fun tryToImport() {
        if (uiState.value.vaultBackupToImport.encryption != null) {
            showImportLoading(false)
            publishEvent(ImportExportUiEvent.ShowDecryptionDialog(encryptionSpec = uiState.value.vaultBackupToImport.encryption!!))
        } else {
            importItems(
                items = uiState.value.vaultBackupToImport.items.orEmpty(),
                tags = uiState.value.vaultBackupToImport.tags.orEmpty(),
            )
        }
    }

    private fun importItems(
        items: List<Item>,
        tags: List<Tag>,
    ) {
        importJob = launchScoped {
            runSafely {
                itemsRepository.importItems(items)
                tagsRepository.importTags(tags)

                vaultsRepository.setUpdatedTimestamp(
                    id = vaultsRepository.getVault().id,
                    timestamp = timeProvider.currentTimeUtc(),
                )
            }
                .onSuccess {
                    showImportLoading(false)
                    publishEvent(ImportExportUiEvent.ImportSuccess)
                }
                .onFailure {
                    showImportLoading(false)
                    publishEvent(ImportExportUiEvent.ShowErrorDialog)
                }
        }
    }

    fun cancelImport() {
        showImportLoading(false)
        importJob?.cancel()
    }

    private fun showImportLoading(show: Boolean) {
        uiState.update { it.copy(importLoading = show) }
    }
}