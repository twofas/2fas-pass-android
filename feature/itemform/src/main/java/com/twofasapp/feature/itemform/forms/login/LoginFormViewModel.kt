/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform.forms.login

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.domain.IconType
import com.twofasapp.core.common.domain.ItemUri
import com.twofasapp.core.common.domain.PasswordGenerator
import com.twofasapp.core.common.domain.PasswordGeneratorSettings
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.data.main.ItemsRepository
import com.twofasapp.data.main.TagsRepository
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

internal class LoginFormViewModel(
    private val vaultsRepository: VaultsRepository,
    private val itemsRepository: ItemsRepository,
    private val settingsRepository: SettingsRepository,
    private val tagsRepository: TagsRepository,
) : ViewModel() {

    private var labelChangedByUser = false
    val uiState = MutableStateFlow(LoginFormUiState())

    init {
        launchScoped {
            val usernameSuggestions = itemsRepository.getMostCommonUsernames()
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

    fun init(
        initialItem: Item,
        initialItemContent: ItemContent.Login,
    ) {
        // TODO: Refactor when new item type added
        launchScoped {
            val itemContent = if (initialItem.id.isBlank()) {
                initialItemContent.copy(
                    username = if (initialItemContent.username.isNullOrBlank()) {
                        itemsRepository.getMostCommonUsernames().firstOrNull()
                    } else {
                        initialItemContent.username
                    },
                    password = if (initialItemContent.password != null && !(initialItemContent.password as? SecretField.ClearText)?.value.isNullOrBlank()) {
                        initialItemContent.password
                    } else {
                        SecretField.ClearText(
                            PasswordGenerator.generatePassword(
                                settingsRepository.observePasswordGeneratorSettings().first(),
                            ),
                        )
                    },
                )
            } else {
                initialItemContent
            }

            val item = if (initialItem.id.isBlank()) {
                initialItem.copy(
                    securityType = settingsRepository.observeDefaultSecurityType().first(),
                    content = itemContent,
                )
            } else {
                initialItem
            }

            uiState.update {
                it.copy(
                    initialised = true,
                    initialItem = item,
                    item = item,
                    initialItemContent = itemContent,
                    itemContent = itemContent,
                )
            }
        }
    }

    private fun updateItem(action: (Item) -> Item) {
        uiState.update { state -> state.copy(item = action(state.item)) }
    }

    private fun updateContent(action: (ItemContent.Login) -> ItemContent.Login) {
        uiState.update { state ->
            val updatedContent = action(state.itemContent)

            state.copy(
                itemContent = updatedContent,
                item = state.item.copy(content = updatedContent),
            )
        }
    }

    fun updateName(text: String) {
        updateContent { content -> content.copy(name = text) }
    }

    fun updateUsername(text: String) {
        updateContent { content -> content.copy(username = text) }
    }

    fun updatePassword(text: String) {
        updateContent { content -> content.copy(password = SecretField.ClearText(text)) }
    }

    fun updatePasswordSettings(settings: PasswordGeneratorSettings) {
        launchScoped {
            settingsRepository.setPasswordGeneratorSettings(settings)
        }
    }

    fun updateIconType(iconType: IconType) {
        updateContent { content -> content.copy(iconType = iconType) }
    }

    fun updateIconUriIndex(index: Int?) {
        updateContent { content -> content.copy(iconUriIndex = index) }
    }

    fun updateLabelText(text: String?) {
        labelChangedByUser = true
        updateContent { content -> content.copy(labelText = text) }
    }

    fun updateLabelColor(text: String?) {
        labelChangedByUser = true
        updateContent { content -> content.copy(labelColor = text) }
    }

    fun updateImageUrl(text: String?) {
        updateContent { content -> content.copy(customImageUrl = text) }
    }

    fun updateUri(index: Int, uri: ItemUri) {
        updateContent { login ->
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
        updateContent { password ->
            password.copy(
                uris = password.uris.plus(ItemUri("")),
            )
        }
    }

    fun deleteUri(index: Int) {
        updateContent { content ->
            content.deleteUri(index)
        }
    }

    fun updateSecurityLevel(securityLevel: SecurityType) {
        updateItem { item -> item.copy(securityType = securityLevel) }
    }

    fun updateTags(tags: List<String>) {
        updateItem { item -> item.copy(tagIds = tags) }
    }

    fun updateNotes(notes: String) {
        updateContent { content -> content.copy(notes = notes) }
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
}