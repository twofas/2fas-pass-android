/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.lock.ui.lock

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.android.ktx.runSafely
import com.twofasapp.core.android.ktx.tickerFlow
import com.twofasapp.core.common.auth.AuthStatusTracker
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.ktx.decodeHex
import com.twofasapp.core.common.ktx.encodeHex
import com.twofasapp.core.common.time.TimeProvider
import com.twofasapp.core.locale.Strings
import com.twofasapp.data.main.SecurityRepository
import com.twofasapp.data.main.VaultKeysRepository
import com.twofasapp.data.settings.SessionRepository
import com.twofasapp.data.settings.SettingsRepository
import com.twofasapp.data.settings.domain.FailedAppUnlocks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import java.util.Locale

internal class LockViewModel(
    private val strings: Strings,
    private val settingsRepository: SettingsRepository,
    private val sessionRepository: SessionRepository,
    private val securityRepository: SecurityRepository,
    private val vaultKeysRepository: VaultKeysRepository,
    private val authStatusTracker: AuthStatusTracker,
    private val timeProvider: TimeProvider,
) : ViewModel() {
    val uiState = MutableStateFlow(LockUiState())

    init {
        launchScoped {
            settingsRepository.observeSelectedTheme().collect { theme ->
                uiState.update { it.copy(selectedTheme = theme) }
            }
        }

        launchScoped {
            settingsRepository.observeDynamicColors().collect { dynamicColors ->
                uiState.update { it.copy(dynamicColors = dynamicColors) }
            }
        }

        launchScoped {
            securityRepository.observeBiometricsEnabled().collect { biometricsEnabled ->
                uiState.update { it.copy(biometricsEnabled = biometricsEnabled) }
            }
        }

        launchScoped {
            sessionRepository.observeBiometricsPrompted().collect { prompted ->
                uiState.update { it.copy(biometricsPrompted = prompted) }
            }
        }

        launchScoped {
            securityRepository.observeMasterKeyEncryptedWithBiometrics().collect { masterKeyEncryptedWithBiometrics ->
                uiState.update { it.copy(masterKeyEncryptedWithBiometrics = masterKeyEncryptedWithBiometrics) }
            }
        }

        launchScoped {
            combine(
                settingsRepository.observeAppLockAttempts(),
                sessionRepository.observeFailedAppUnlocks(),
                tickerFlow(1000),
            ) { a, b, _ -> Pair(a, b) }
                .collect { (appLockAttempts, failedAppUnlocks) ->
                    uiState.update {
                        it.copy(
                            appLockAttempts = appLockAttempts,
                            failedAppUnlocks = failedAppUnlocks,
                        )
                    }

                    if (failedAppUnlocks == null || appLockAttempts.maxAttempts == null) {
                        uiState.update { it.copy(locked = false) }
                        return@collect
                    }

                    val lockoutDuration = failedAppUnlocks.lockoutDuration
                    val lockoutUntil = failedAppUnlocks.lastFailedAttemptSinceBoot + lockoutDuration
                    val isLockoutElapsed = lockoutUntil - timeProvider.systemElapsedTime() <= 0

                    if (lockoutDuration > 0 && isLockoutElapsed.not()) {
                        uiState.update {
                            it.copy(
                                locked = true,
                                passwordError = strings.lockScreenTryAgainIn.format(formatMillisCountdown(lockoutUntil - timeProvider.systemElapsedTime())),
                            )
                        }
                    } else {
                        uiState.update {
                            it.copy(
                                locked = false,
                                passwordError = if (lockoutDuration == 0L) it.passwordError else null,
                            )
                        }
                    }
                }
        }
    }

    fun unlockWithPassword(password: String, onSuccess: (ByteArray) -> Unit) {
        uiState.update { it.copy(loading = true, passwordError = null) }

        launchScoped {
            runSafely {
                val masterKey = securityRepository.getMasterKeyWithPassword(password)
                vaultKeysRepository.generateAndSaveVaultKeys(masterKey)
                masterKey
            }
                .onSuccess { masterKey ->
                    resetFailedAttempts()
                    uiState.update { it.copy(loading = false) }
                    onSuccess(masterKey.decodeHex())
                }
                .onFailure {
                    incrementFailedAttempt()
                    uiState.update { it.copy(loading = false, passwordError = "Invalid password") }
                }
        }
    }

    fun unlockWithBiometrics(masterKey: ByteArray) {
        uiState.update { it.copy(loading = true, passwordError = null) }

        launchScoped {
            runSafely {
                vaultKeysRepository.generateAndSaveVaultKeys(masterKey.encodeHex())
            }
                .onSuccess {
                    resetFailedAttempts()
                    finishWithSuccess()
                }
                .onFailure {
                    incrementFailedAttempt()
                    uiState.update { it.copy(loading = false, passwordError = "Invalid biometrics") }
                }
        }
    }

    fun biometricsInvalidated() {
        launchScoped {
            securityRepository.saveBiometricsEnabled(false)
            securityRepository.saveMasterKeyEncryptedWithBiometrics(null)
        }
    }

    fun biometricsPrompted() {
        launchScoped {
            sessionRepository.setBiometricsPrompted(true)
        }
    }

    private fun incrementFailedAttempt() {
        launchScoped {
            val maxAttempts = uiState.value.appLockAttempts.maxAttempts ?: return@launchScoped
            val currentFailedAppUnlocks = uiState.value.failedAppUnlocks ?: FailedAppUnlocks.Empty
            val newFailedAttempts = minOf(currentFailedAppUnlocks.failedAttempts + 1, maxAttempts)

            val newFailedAppUnlocks = if (newFailedAttempts >= maxAttempts) {
                currentFailedAppUnlocks.copy(
                    lockoutCount = currentFailedAppUnlocks.lockoutCount + 1,
                    failedAttempts = newFailedAttempts,
                    lastFailedAttemptSinceBoot = timeProvider.systemElapsedTime(),
                )
            } else {
                currentFailedAppUnlocks.copy(
                    lockoutCount = 0,
                    failedAttempts = currentFailedAppUnlocks.failedAttempts + 1,
                    lastFailedAttemptSinceBoot = timeProvider.systemElapsedTime(),
                )
            }

            sessionRepository.setFailedAppUnlocks(newFailedAppUnlocks)
        }
    }

    private fun resetFailedAttempts() {
        launchScoped {
            sessionRepository.setFailedAppUnlocks(null)
        }
    }

    private fun formatMillisCountdown(millis: Long): String {
        val totalSeconds = (millis / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    fun finishWithSuccess() {
        launchScoped {
            authStatusTracker.authenticate()
        }
    }

    fun finishWithBiometricsEnabled(encryptedMasterKey: EncryptedBytes) {
        launchScoped {
            securityRepository.saveMasterKeyEncryptedWithBiometrics(encryptedMasterKey)
            securityRepository.saveBiometricsEnabled(true)
            authStatusTracker.authenticate()
        }
    }
}