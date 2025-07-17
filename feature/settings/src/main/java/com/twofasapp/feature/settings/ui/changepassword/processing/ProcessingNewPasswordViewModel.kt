/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.changepassword.processing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.android.ktx.runSafely
import com.twofasapp.core.android.navigation.Screen
import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.crypto.AndroidKeyStore
import com.twofasapp.core.common.crypto.decrypt
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.core.common.services.CrashlyticsInstance
import com.twofasapp.data.main.CloudRepository
import com.twofasapp.data.main.LoginsRepository
import com.twofasapp.data.main.SecurityRepository
import com.twofasapp.data.main.TagsRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.VaultKeysRepository
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class ProcessingNewPasswordViewModel(
    savedStateHandle: SavedStateHandle,
    dispatchers: Dispatchers,
    private val securityRepository: SecurityRepository,
    private val androidKeyStore: AndroidKeyStore,
    private val loginsRepository: LoginsRepository,
    private val vaultsRepository: VaultsRepository,
    private val tagsRepository: TagsRepository,
    private val vaultKeysRepository: VaultKeysRepository,
    private val vaultCryptoScope: VaultCryptoScope,
    private val cloudRepository: CloudRepository,
    private val itemEncryptionMapper: ItemEncryptionMapper,
) : ViewModel() {
    val uiState = MutableStateFlow(ProcessingNewPasswordUiState())

    init {
        launchScoped(dispatchers.io) {
            runSafely {
                uiState.update {
                    it.copy(
                        step = ProcessingNewPasswordUiState.Step.Processing,
                        processingMessage = "Re-encrypting local data...",
                    )
                }

                // Re-encrypt local database
                val vault = vaultsRepository.getVault()
                val logins = loginsRepository.getLoginsDecryptedWithDeleted()

                val encryptedPassword: String = savedStateHandle.toRoute<Screen.ProcessingNewPassword>().encryptedPassword
                val password = decrypt(androidKeyStore.appKey, EncryptedBytes(encryptedPassword.decodeBase64())).decodeToString()
                val masterKey = securityRepository.generateMasterKey(
                    password = password,
                    seed = securityRepository.getSeed(),
                    kdfSpec = securityRepository.getMasterKeyKdfSpec(),
                )
                uiState.update { it.copy(newMasterKeyHex = masterKey.hashHex) }

                val newVaultKeys = vaultKeysRepository.generateVaultKeys(masterKeyHex = masterKey.hashHex, vaultId = vault.id)
                val newLogins = vaultCryptoScope.withVaultCipher(vaultKeys = newVaultKeys) {
                    itemEncryptionMapper.encryptLogins(logins = logins, vaultCipher = this)
                }

                loginsRepository.lockLogins()
                loginsRepository.saveLogins(newLogins)
                tagsRepository.reencryptTags(newVaultKeys)
                vaultKeysRepository.generateAndSaveVaultKeys(masterKeyHex = masterKey.hashHex)
                securityRepository.saveEncryptionReference(masterKey)
                securityRepository.saveBiometricsEnabled(false)
                loginsRepository.unlockLogins()

                if (cloudRepository.getSyncInfo().enabled.not()) {
                    delay(1500)
                    uiState.update { it.copy(step = ProcessingNewPasswordUiState.Step.Success) }
                    return@launchScoped
                }

                uiState.update {
                    it.copy(
                        step = ProcessingNewPasswordUiState.Step.Processing,
                        processingMessage = "Syncing cloud...",
                    )
                }
                cloudRepository.sync(forceReplace = true)
                delay(1500)
                uiState.update { it.copy(step = ProcessingNewPasswordUiState.Step.Success) }
            }
                .onFailure { e ->
                    uiState.update { state -> state.copy(step = ProcessingNewPasswordUiState.Step.Error(e.message.orEmpty())) }
                    CrashlyticsInstance.logException(e)
                }
        }
    }
}