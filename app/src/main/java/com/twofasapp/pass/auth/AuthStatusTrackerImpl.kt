/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.auth

import com.twofasapp.core.common.auth.AuthStatusTracker
import com.twofasapp.core.common.domain.AuthStatus
import com.twofasapp.core.common.time.TimeProvider
import com.twofasapp.data.main.VaultKeysRepository
import com.twofasapp.data.settings.SettingsRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

internal class AuthStatusTrackerImpl(
    private val settingsRepository: SettingsRepository,
    private val vaultKeysRepository: VaultKeysRepository,
    private val timeProvider: TimeProvider,
) : AuthStatusTracker {

    private val authStatusFlow = MutableStateFlow<AuthStatus>(AuthStatus.Invalid.NotAuthenticated)

    private var lastForegroundTime = timeProvider.systemElapsedTime()
    private var lastBackgroundTime = 0L

    init {
        @Suppress("OPT_IN_USAGE")
        GlobalScope.launch {
            vaultKeysRepository.observeHasValidVaultKeys().collect { hasValidVaultKeys ->
                if (hasValidVaultKeys.not()) {
                    authStatusFlow.emit(AuthStatus.Invalid.NotAuthenticated)
                }
            }
        }
    }

    override fun observeAuthStatus(): Flow<AuthStatus> {
        return authStatusFlow
            .onEach { Timber.tag("AuthStatusTracker").i(it.toString()) }
    }

    override fun observeIsAuthenticated(): Flow<Boolean> {
        return authStatusFlow.map { it.isAuthenticated() }
    }

    override suspend fun isAuthenticated(): Boolean {
        return authStatusFlow.value.isAuthenticated()
    }

    override suspend fun authenticate() {
        authStatusFlow.emit(AuthStatus.Valid.Authenticated)
    }

    override suspend fun onAppForeground() {
        lastForegroundTime = timeProvider.systemElapsedTime()

        authStatusFlow.emit(evaluateAuthStatus())
    }

    override suspend fun onAppBackground() {
        if (authStatusFlow.value is AuthStatus.Valid) {
            lastBackgroundTime = timeProvider.systemElapsedTime()
            authStatusFlow.emit(AuthStatus.Invalid.AppBackgrounded)
        }
    }

    private suspend fun getAppLockTimeout(): Long {
        return settingsRepository.observeAppLockTime().first().millis
    }

    private suspend fun evaluateAuthStatus(): AuthStatus {
        if (vaultKeysRepository.observeHasValidVaultKeys().first().not()) {
            return AuthStatus.Invalid.NotAuthenticated
        }

        Timber.tag("AuthStatusTracker").i(
            buildString {
                append("diff=${lastForegroundTime - lastBackgroundTime} | getAppLockTimeout=${getAppLockTimeout()}")
                append(" (lastForegroundTime=$lastForegroundTime, lastBackgroundTime=$lastBackgroundTime)")
            },
        )

        return if (lastForegroundTime - lastBackgroundTime > getAppLockTimeout()) {
            AuthStatus.Invalid.SessionExpired
        } else {
            AuthStatus.Valid.SessionValid
        }
    }

    private fun AuthStatus.isAuthenticated(): Boolean {
        return when (this) {
            is AuthStatus.Valid -> {
                true
            }

            is AuthStatus.Invalid -> {
                when (this) {
                    AuthStatus.Invalid.AppBackgrounded -> true
                    AuthStatus.Invalid.NotAuthenticated -> false
                    AuthStatus.Invalid.SessionExpired -> false
                }
            }
        }
    }
}