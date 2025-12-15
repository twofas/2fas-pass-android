/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform.forms.login

import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.domain.IconType
import com.twofasapp.core.common.domain.ItemUri
import com.twofasapp.core.common.domain.PasswordGenerator
import com.twofasapp.core.common.domain.PasswordGeneratorSettings
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.data.main.ItemsRepository
import com.twofasapp.data.main.TagsRepository
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.settings.SettingsRepository
import com.twofasapp.feature.itemform.ItemFormViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

internal class LoginFormViewModel(
    private val itemsRepository: ItemsRepository,
    private val settingsRepository: SettingsRepository,
    vaultsRepository: VaultsRepository,
    tagsRepository: TagsRepository,
) : ItemFormViewModel<ItemContent.Login>(
    vaultsRepository = vaultsRepository,
    settingsRepository = settingsRepository,
    tagsRepository = tagsRepository,
) {

    private var labelChangedByUser = false
    val loginUiState = MutableStateFlow(LoginFormUiState())

    init {
        launchScoped {
            val usernameSuggestions = itemsRepository.getMostCommonUsernames()

            loginUiState.update { state ->
                state.copy(
                    usernameSuggestions = usernameSuggestions,
                    usernameSuggestionsFiltered = usernameSuggestions.filterUsernameSuggestions(itemState.value.itemContent?.username),
                )
            }
        }

        launchScoped {
            settingsRepository.observePasswordGeneratorSettings().collect { settings ->
                loginUiState.update { it.copy(passwordGeneratorSettings = settings) }
            }
        }
    }

    override suspend fun preInitItemContent(content: ItemContent.Login): ItemContent.Login {
        return content.copy(
            username = if (content.username == null) {
                itemsRepository.getMostCommonUsernames().firstOrNull()
            } else {
                content.username
            },
            password = if (content.password != null) {
                content.password
            } else {
                SecretField.ClearText(
                    PasswordGenerator.generatePassword(
                        settingsRepository.observePasswordGeneratorSettings().first(),
                    ),
                )
            },
        )
    }

    fun updateName(text: String) {
        updateItemContent { content -> content.copy(name = text) }
    }

    fun updateUsername(text: String) {
        updateItemContent { content -> content.copy(username = text) }

        loginUiState.update { state ->
            state.copy(
                usernameSuggestionsFiltered = state.usernameSuggestions.filterUsernameSuggestions(text),
            )
        }
    }

    fun updatePassword(text: String) {
        updateItemContent { content -> content.copy(password = SecretField.ClearText(text)) }
    }

    fun updatePasswordSettings(settings: PasswordGeneratorSettings) {
        launchScoped {
            settingsRepository.setPasswordGeneratorSettings(settings)
        }
    }

    fun updateIconType(iconType: IconType) {
        updateItemContent { content -> content.copy(iconType = iconType) }
    }

    fun updateIconUriIndex(index: Int?) {
        updateItemContent { content -> content.copy(iconUriIndex = index) }
    }

    fun updateLabelText(text: String?) {
        labelChangedByUser = true
        updateItemContent { content -> content.copy(labelText = text) }
    }

    fun updateLabelColor(text: String?) {
        labelChangedByUser = true
        updateItemContent { content -> content.copy(labelColor = text) }
    }

    fun updateImageUrl(text: String?) {
        updateItemContent { content -> content.copy(customImageUrl = text) }
    }

    fun updateUri(index: Int, uri: ItemUri) {
        updateItemContent { login ->
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
        updateItemContent { password ->
            password.copy(
                uris = password.uris.plus(ItemUri("")),
            )
        }
    }

    fun deleteUri(index: Int) {
        updateItemContent { content ->
            content.deleteUri(index)
        }
    }

    fun updateNotes(notes: String) {
        updateItemContent { content -> content.copy(notes = notes) }
    }

    private fun ItemContent.Login.deleteUri(index: Int): ItemContent.Login {
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

    private fun List<String>.filterUsernameSuggestions(username: String?): List<String> {
        return filter { it.contains(username.orEmpty().trim(), false) }
            .distinctBy { it.trim().lowercase() }
            .take(8)
    }
}