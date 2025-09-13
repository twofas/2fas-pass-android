/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.backupdecryption

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.android.ktx.runSafely
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.domain.crypto.EncryptionSpec
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.core.common.ktx.decodeHex
import com.twofasapp.core.common.ktx.decodeUrlParam
import com.twofasapp.core.common.ktx.encodeHex
import com.twofasapp.core.common.ktx.readPdfAsBitmap
import com.twofasapp.data.main.BackupRepository
import com.twofasapp.data.main.ItemsRepository
import com.twofasapp.data.main.SecurityRepository
import com.twofasapp.data.main.TagsRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.VaultKeysRepository
import com.twofasapp.data.main.domain.VaultBackup
import com.twofasapp.data.security.crypto.MasterKey
import com.twofasapp.data.security.crypto.Seed
import com.twofasapp.feature.decryptionkit.generator.DecryptionKit
import com.twofasapp.feature.importvault.ui.ImportVaultState
import com.twofasapp.feature.qrscan.ReadQrFromImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.crypto.AEADBadTagException

class BackupDecryptionViewModel(
    private val securityRepository: SecurityRepository,
    private val vaultKeysRepository: VaultKeysRepository,
    private val vaultCryptoScope: VaultCryptoScope,
    private val readQrFromImage: ReadQrFromImage,
    private val backupRepository: BackupRepository,
    private val itemsRepository: ItemsRepository,
    private val tagsRepository: TagsRepository,
) : ViewModel() {
    val uiState = MutableStateFlow(BackupDecryptionUiState())

    fun init(vaultBackup: VaultBackup) {
        uiState.update { it.copy(vaultBackup = vaultBackup) }
        setInitialStep(vaultBackup)
    }

    private fun setInitialStep(vaultBackup: VaultBackup) {
        launchScoped {
            val localSeed = securityRepository.getSeed()
            val localSeedValid = checkHash(vaultId = vaultBackup.vaultId, spec = vaultBackup.encryption!!, seed = localSeed)
            uiState.update { it.copy(localSeedValid = localSeedValid) }

            if (localSeedValid) {
                uiState.update { it.copy(seed = localSeed) }
                openState(ImportVaultState.EnterMasterPassword)
            } else {
                openState(ImportVaultState.Default)
            }
        }
    }

    fun openState(state: ImportVaultState) {
        uiState.update { it.copy(state = state) }
    }

    fun updateWords(words: List<String>) {
        uiState.update { it.copy(words = words) }
    }

    fun dismissSeedError() {
        uiState.update { it.copy(seedError = null) }
    }

    fun updateMasterKey(masterKyeHex: String) {
        launchScoped {
            uiState.update {
                it.copy(
                    masterKeyHex = masterKyeHex,
                )
            }
        }
    }

    fun restoreFromWords(words: List<String>) {
        launchScoped {
            runSafely {
                val seed = securityRepository.restoreSeed(words.map { it.trim().lowercase() })
                uiState.update { it.copy(seed = seed) }

                if (checkSeed()) {
                    import()
                } else {
                    uiState.update { it.copy(seedError = "Entered Secret Key does not match imported Vault. Please make sure you have entered correct words.") }
                }
            }.onFailure {
                uiState.update { it.copy(seedError = "Entered Secret Key does not match imported Vault. Please make sure you have entered correct words.") }
            }
        }
    }

    fun readDecryptionKit(context: Context, fileUri: Uri) {
        openState(ImportVaultState.ImportingFile)

        launchScoped {
            context.readPdfAsBitmap(fileUri)?.let { bitmap ->
                readQrFromImage.invoke(
                    bitmap = bitmap,
                )
                    .onSuccess { uriString ->
                        val uri = uriString.toUri()
                        val decryptionKit = DecryptionKit(
                            words = emptyList(),
                            entropy = uri.getQueryParameter("entropy")!!.decodeUrlParam().decodeBase64(),
                            masterKey = uri.getQueryParameter("master_key")?.decodeUrlParam()?.decodeBase64(),
                        )

                        uiState.update { state ->
                            state.copy(
                                seed = securityRepository.generateSeed(decryptionKit.entropy),
                                masterKeyHex = decryptionKit.masterKey?.encodeHex(),
                            )
                        }

                        if (checkSeed()) {
                            import()
                        } else {
                            openState(
                                ImportVaultState.ImportingFileError(
                                    title = "Error reading Decryption Kit",
                                    msg = "Selected Decryption Kit does not match imported Vault. Please select a different Decryption Kit file.",
                                ),
                            )
                        }
                    }
                    .onFailure { e ->
                        e.printStackTrace()
                        openState(
                            ImportVaultState.ImportingFileError(
                                title = "Error reading Decryption Kit",
                                msg = "Error occurred while reading Decryption Kit.",
                            ),
                        )
                    }
            } ?: openState(
                ImportVaultState.ImportingFileError(
                    title = "Error reading Decryption Kit",
                    msg = "Error occurred while reading Decryption Kit.",
                ),
            )
        }
    }

    fun readDecryptionKitFromQr(text: String) {
        Timber.d("Scanned: $text")
        openState(ImportVaultState.ImportingFile)

        launchScoped {
            runSafely {
                DecryptionKit.readQrCodeContent(text)
            }.onSuccess { decryptionKit ->
                uiState.update {
                    it.copy(
                        seed = securityRepository.generateSeed(decryptionKit.entropy),
                        masterKeyHex = decryptionKit.masterKey?.encodeHex(),
                    )
                }

                if (checkSeed()) {
                    import()
                } else {
                    openState(
                        ImportVaultState.ImportingFileError(
                            title = "Error scanning Decryption Kit",
                            msg = "Scanned Decryption Kit does not match imported Vault. Please scan a different Decryption Kit.",
                        ),
                    )
                }
            }.onFailure {
                openState(
                    ImportVaultState.ImportingFileError(
                        title = "Error scanning Decryption Kit",
                        msg = "There was an error when scanning Decryption Kit QR Code. Try again or select a different Decryption Kit.\n\n(${it.message})",
                    ),
                )
            }
        }
    }

    fun resetDecryptionKitData() {
        uiState.update {
            it.copy(
                seed = null,
                masterKeyHex = null,
                words = List(15) { "" },
            )
        }
    }

    fun checkPassword(password: String) {
        uiState.update { it.copy(passwordLoading = true, passwordError = null) }

        launchScoped {
            runSafely {
                val masterKey = securityRepository.generateMasterKey(
                    password = password,
                    seed = uiState.value.seed!!,
                    kdfSpec = uiState.value.vaultBackup.encryption!!.kdfSpec,
                )

                val vaultKeys = vaultKeysRepository.generateVaultKeys(
                    masterKeyHex = masterKey.hashHex,
                    vaultId = uiState.value.vaultBackup.vaultId,
                )

                vaultCryptoScope.withVaultCipher(vaultKeys) {
                    decryptWithExternalKey(
                        EncryptedBytes(uiState.value.vaultBackup.encryption!!.reference.decodeBase64()),
                    )
                }

                uiState.update { it.copy(passwordLoading = false, passwordError = null) }

                updateMasterKey(masterKey.hashHex)

                import()
            }.onFailure { e ->
                e.printStackTrace()
                uiState.update { it.copy(passwordLoading = false, passwordError = "The password you entered is incorrect.") }
            }
        }
    }

    fun import() {
        launchScoped {
            if (uiState.value.masterKeyHex == null) {
                openState(ImportVaultState.EnterMasterPassword)
            } else {
                openState(ImportVaultState.ImportingFile)

                runSafely {
                    val seed = uiState.value.seed!!
                    val masterKey = MasterKey(hashHex = uiState.value.masterKeyHex!!)
                    val vaultBackupDecrypted = backupRepository.decryptVaultBackup(
                        vaultBackup = uiState.value.vaultBackup,
                        masterKey = masterKey.hashHex.decodeHex(),
                        seed = seed,
                    )

                    val items = vaultBackupDecrypted.items.orEmpty()
                    val tags = vaultBackupDecrypted.tags.orEmpty()

                    itemsRepository.importItems(items)
                    tagsRepository.importTags(tags)
                }
                    .onSuccess {
                        openState(ImportVaultState.ImportingFileSuccess)
                    }
                    .onFailure { exception ->
                        openState(
                            ImportVaultState.ImportingFileError(
                                title = "Error importing file",
                                msg = when (exception) {
                                    is AEADBadTagException -> "Selected Decryption Kit has a different Master Key than the one used to encrypt your backup file. Please choose a different Decryption Kit or enter the Secret Key and Master Password manually."
                                    else -> exception.message.orEmpty()
                                },
                            ),
                        )
                    }
            }
        }
    }

    private suspend fun checkSeed(): Boolean {
        return try {
            checkHash(
                vaultId = uiState.value.vaultBackup.vaultId,
                spec = uiState.value.vaultBackup.encryption!!,
                seed = uiState.value.seed!!,
            )
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun checkHash(vaultId: String, spec: EncryptionSpec, seed: Seed): Boolean {
        val generatedHash = vaultKeysRepository.generateVaultHashes(seed.seedHex, vaultId).seedHashBase64

        return generatedHash == spec.seedHash
    }
}