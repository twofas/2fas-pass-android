/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.restorevault.decyptvault

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.android.ktx.runSafely
import com.twofasapp.core.common.auth.AuthStatusTracker
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.domain.crypto.EncryptionSpec
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.core.common.ktx.decodeHex
import com.twofasapp.core.common.ktx.decodeUrlParam
import com.twofasapp.core.common.ktx.encodeHex
import com.twofasapp.core.common.ktx.readPdfAsBitmap
import com.twofasapp.data.cloud.services.CloudServiceProvider
import com.twofasapp.data.main.BackupRepository
import com.twofasapp.data.main.CloudRepository
import com.twofasapp.data.main.DeletedItemsRepository
import com.twofasapp.data.main.LoginsRepository
import com.twofasapp.data.main.SecurityRepository
import com.twofasapp.data.main.TagsRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.VaultKeysRepository
import com.twofasapp.data.main.domain.CloudSyncInfo
import com.twofasapp.data.security.crypto.MasterKey
import com.twofasapp.data.security.crypto.Seed
import com.twofasapp.data.settings.SessionRepository
import com.twofasapp.feature.decryptionkit.generator.DecryptionKit
import com.twofasapp.feature.importvault.ui.ImportVaultState
import com.twofasapp.feature.qrscan.ReadQrFromImage
import com.twofasapp.feature.startup.ui.StartupConfig
import com.twofasapp.feature.startup.ui.restorevault.RestoreFile
import com.twofasapp.feature.startup.ui.restorevault.RestoreSource
import com.twofasapp.feature.startup.ui.restorevault.RestoreState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import java.time.Instant
import javax.crypto.AEADBadTagException

internal class DecryptVaultViewModel(
    private val startupConfig: StartupConfig,
    private val vaultKeysRepository: VaultKeysRepository,
    private val securityRepository: SecurityRepository,
    private val loginsRepository: LoginsRepository,
    private val tagsRepository: TagsRepository,
    private val deletedItemsRepository: DeletedItemsRepository,
    private val backupRepository: BackupRepository,
    private val vaultCryptoScope: VaultCryptoScope,
    private val sessionRepository: SessionRepository,
    private val authStatusTracker: AuthStatusTracker,
    private val cloudRepository: CloudRepository,
    private val cloudServiceProvider: CloudServiceProvider,
    private val readQrFromImage: ReadQrFromImage,
    private val restoreState: RestoreState,
) : ViewModel() {

    val uiState = MutableStateFlow(
        DecryptVaultUiState(),
    )

    init {
        launchScoped(Dispatchers.IO) {
            runSafely {
                delay(300)

                when (restoreState.restoreFile) {
                    is RestoreFile.Cloud -> {
                        val backupContent = cloudServiceProvider.provide(restoreState.cloudConfig!!).fetchFile(
                            config = restoreState.cloudConfig!!,
                            info = (restoreState.restoreFile as RestoreFile.Cloud).fileInfo,
                        )

                        backupRepository.readVaultBackup(backupContent)
                    }

                    is RestoreFile.LocalFile -> {
                        backupRepository.readVaultBackup(fileUri = (restoreState.restoreFile as RestoreFile.LocalFile).uri)
                    }

                    null -> throw RuntimeException("Invalid config!")
                }
            }
                .onSuccess { vaultBackup ->
                    if (vaultBackup.encryption == null) {
                        uiState.update {
                            it.copy(
                                screenState = ImportVaultState.ReadingFileError(
                                    title = "Backup is unencrypted",
                                    msg = "The vault cannot be recovered from an unencrypted backup file. To import it, create a new vault and navigate to Settings -> Import/Export.",
                                ),
                            )
                        }
                    } else {
                        uiState.update {
                            it.copy(
                                encryptedBackup = vaultBackup,
                                screenState = ImportVaultState.Default,
                            )
                        }
                    }
                }
                .onFailure { e ->
                    uiState.update {
                        it.copy(
                            screenState = ImportVaultState.ReadingFileError(
                                title = "Error reading file",
                                msg = e.message.orEmpty(),
                            ),
                        )
                    }
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
                            restore()
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
                    restore()
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

    fun restore() {
        launchScoped {
            if (uiState.value.masterKeyHex == null) {
                openState(ImportVaultState.EnterMasterPassword)
            } else {
                openState(ImportVaultState.ImportingFile)

                runSafely {
                    startupConfig.clearStorage()

                    val seed = uiState.value.seed!!
                    val masterKey = MasterKey(hashHex = uiState.value.masterKeyHex!!)
                    val backup = uiState.value.encryptedBackup
                    val decryptedBackup = backupRepository.decryptVaultBackup(
                        vaultBackup = backup,
                        masterKey = masterKey.hashHex.decodeHex(),
                        seed = seed,
                    )

                    val logins = decryptedBackup.logins.orEmpty()
                    val tags = decryptedBackup.tags.orEmpty()
                    val deletedItems = decryptedBackup.deletedItems.orEmpty()

                    startupConfig.seed = seed
                    startupConfig.masterKey = masterKey
                    startupConfig.finishStartup(
                        vaultId = backup.vaultId,
                        vaultName = backup.vaultName,
                        vaultCreatedAt = backup.vaultCreatedAt,
                        vaultUpdatedAt = backup.vaultUpdatedAt,
                    )

                    securityRepository.saveEncryptionReference(masterKey) // Important!

                    loginsRepository.importLogins(logins, triggerSync = false)
                    tagsRepository.importTags(tags)
                    deletedItemsRepository.saveDeletedItems(deletedItems)

                    when (restoreState.restoreSource) {
                        RestoreSource.LocalFile -> Unit
                        RestoreSource.GoogleDrive,
                        RestoreSource.WebDav,
                        -> {
                            cloudRepository.setSyncInfo(
                                CloudSyncInfo(
                                    enabled = true,
                                    config = restoreState.cloudConfig!!,
                                    lastSuccessfulSyncTime = Instant.now().toEpochMilli(),
                                ),
                            )

                            cloudRepository.sync(forceReplace = true)
                        }
                    }

                    restoreState.reset()
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

    fun restoreFromWords(words: List<String>) {
        launchScoped {
            runSafely {
                val seed = securityRepository.restoreSeed(words.map { it.trim().lowercase() })
                uiState.update { it.copy(seed = seed) }

                if (checkSeed()) {
                    restore()
                } else {
                    uiState.update { it.copy(seedError = "Entered Secret Key does not match imported Vault. Please make sure you have entered correct words.") }
                }
            }.onFailure {
                uiState.update { it.copy(seedError = "Entered Secret Key does not match imported Vault. Please make sure you have entered correct words.") }
            }
        }
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

    fun finishWithSuccess() {
        launchScoped {
            authStatusTracker.authenticate()
            sessionRepository.setStartupCompleted(true)
        }
    }

    fun openState(state: ImportVaultState) {
        uiState.update { it.copy(screenState = state) }
    }

    private suspend fun checkSeed(): Boolean {
        return try {
            checkHash(
                vaultId = uiState.value.encryptedBackup.vaultId,
                spec = uiState.value.encryptedBackup.encryption!!,
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

    fun checkPassword(password: String) {
        uiState.update { it.copy(passwordLoading = true, passwordError = null) }

        launchScoped {
            runSafely {
                val masterKey = securityRepository.generateMasterKey(
                    password = password,
                    seed = uiState.value.seed!!,
                    kdfSpec = uiState.value.encryptedBackup.encryption!!.kdfSpec,
                )

                val vaultKeys = vaultKeysRepository.generateVaultKeys(
                    masterKeyHex = masterKey.hashHex,
                    vaultId = uiState.value.encryptedBackup.vaultId,
                )

                vaultCryptoScope.withVaultCipher(vaultKeys) {
                    decryptWithExternalKey(
                        EncryptedBytes(uiState.value.encryptedBackup.encryption!!.reference.decodeBase64()),
                    )
                }

                uiState.update { it.copy(passwordLoading = false, passwordError = null) }

                updateMasterKey(masterKey.hashHex)

                restore()
            }.onFailure { e ->
                e.printStackTrace()
                uiState.update { it.copy(passwordLoading = false, passwordError = "The password you entered is incorrect.") }
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
}