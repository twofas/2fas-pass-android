/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.loginform.ui

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.domain.IconType
import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.common.domain.LoginUri
import com.twofasapp.core.common.domain.PasswordGenerator
import com.twofasapp.core.common.domain.PasswordGeneratorSettings
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.data.main.LoginsRepository
import com.twofasapp.data.main.TagsRepository
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

internal class LoginFormViewModel(
    private val vaultsRepository: VaultsRepository,
    private val loginsRepository: LoginsRepository,
    private val settingsRepository: SettingsRepository,
    private val tagsRepository: TagsRepository,
) : ViewModel() {

    private var labelChangedByUser = false
    val uiState = MutableStateFlow(LoginFormUiState())

    init {
        launchScoped {
            val usernameSuggestions = loginsRepository.getMostCommonUsernames()
            uiState.update { it.copy(usernameSuggestions = usernameSuggestions) }
        }

        launchScoped {
            settingsRepository.observePasswordGeneratorSettings().collect { settings ->
                uiState.update { it.copy(passwordGeneratorSettings = settings) }
            }
        }

        launchScoped {
            tagsRepository.observeTags(vaultsRepository.getVault().id).collect { tags ->
                uiState.update { it.copy(tags = tags) }
            }
        }
    }

    fun initLogin(initialLogin: Login) {
        launchScoped {
            val login = if (initialLogin.id.isBlank()) {
                initialLogin.copy(
                    securityType = settingsRepository.observeDefaultSecurityType().first(),
                    username = if (initialLogin.username.isNullOrBlank()) {
                        loginsRepository.getMostCommonUsernames().firstOrNull()
                    } else {
                        initialLogin.username
                    },
                    password = if (initialLogin.password != null && !(initialLogin.password as? SecretField.Visible)?.value.isNullOrBlank()) {
                        initialLogin.password
                    } else {
                        SecretField.Visible(
                            PasswordGenerator.generatePassword(
                                settingsRepository.observePasswordGeneratorSettings().first(),
                            ),
                        )
                    },
                )
            } else {
                initialLogin
            }

            uiState.update {
                it.copy(
                    initialised = true,
                    initialLogin = login,
                    login = login,
                )
            }
        }
    }

    private fun updateLogin(action: (Login) -> Login) {
        uiState.update { state -> state.copy(login = action(state.login)) }
    }

    fun updateName(text: String) {
        updateLogin { login -> login.copy(name = text) }
    }

    fun updateUsername(text: String) {
        updateLogin { login -> login.copy(username = text) }
    }

    fun updatePassword(text: String) {
        updateLogin { login -> login.copy(password = SecretField.Visible(text)) }
    }

    fun updatePasswordSettings(settings: PasswordGeneratorSettings) {
        launchScoped {
            settingsRepository.setPasswordGeneratorSettings(settings)
        }
    }

    fun updateIconType(iconType: IconType) {
        updateLogin { login -> login.copy(iconType = iconType) }
    }

    fun updateIconUriIndex(index: Int?) {
        updateLogin { login -> login.copy(iconUriIndex = index) }
    }

    fun updateLabelText(text: String?) {
        labelChangedByUser = true
        updateLogin { login -> login.copy(labelText = text) }
    }

    fun updateLabelColor(text: String?) {
        labelChangedByUser = true
        updateLogin { login -> login.copy(labelColor = text) }
    }

    fun updateImageUrl(text: String?) {
        updateLogin { login -> login.copy(customImageUrl = text) }
    }

    fun updateUri(index: Int, uri: LoginUri) {
        updateLogin { login ->
            login.copy(
                uris = login.uris
                    .mapIndexed { localUriIndex, localUriItem ->
                        if (localUriIndex == index) {
                            uri
                        } else {
                            localUriItem
                        }
                    },
            )
        }
    }

    fun addUri() {
        updateLogin { password ->
            password.copy(
                uris = password.uris.plus(LoginUri("")),
            )
        }
    }

    fun deleteUri(index: Int) {
        updateLogin { login ->
            login.deleteUri(index)
        }
    }

    fun updateSecurityLevel(securityLevel: SecurityType) {
        updateLogin { login -> login.copy(securityType = securityLevel) }
    }

    fun updateTags(tags: List<String>) {
        updateLogin { login -> login.copy(tagIds = tags) }
    }

    fun updateNotes(notes: String) {
        updateLogin { login -> login.copy(notes = notes) }
    }

    private fun Login.deleteUri(index: Int): Login {
        val updatedUris = uris.toMutableList().apply { removeAt(index) }

        return copy(
            uris = updatedUris,
            iconUriIndex = iconUriIndex?.let { iconIndex ->
                val newIndex = if (iconIndex >= index) {
                    iconIndex - 1
                } else {
                    iconIndex
                }

                if (newIndex < 0) {
                    if (updatedUris.isNotEmpty()) {
                        0
                    } else {
                        null
                    }
                } else {
                    newIndex
                }
            },
        )
    }
}