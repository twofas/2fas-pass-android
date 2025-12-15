/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.data.main.TagsRepository
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.settings.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

internal open class ItemFormViewModel<T : ItemContent>(
    private val vaultsRepository: VaultsRepository,
    private val settingsRepository: SettingsRepository,
    private val tagsRepository: TagsRepository,
) : ViewModel() {

    val itemState = MutableStateFlow(ItemFormUiState<T>())

    init {
        updateTagsList()
    }

    fun init(initialItem: Item) {
        launchScoped(Dispatchers.IO) {
            val item = if (initialItem.id.isBlank()) {
                initialItem.copy(
                    securityType = settingsRepository.observeDefaultSecurityType().first(),
                )
            } else {
                initialItem
            }

            @Suppress("UNCHECKED_CAST")
            val itemContent = preInitItemContent(item.content as T)

            itemState.update {
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

    open suspend fun preInitItemContent(content: T): T {
        return content
    }

    fun updateItemContent(action: (T) -> T) {
        itemState.update { state ->
            val updatedContent = action(state.itemContent!!)

            state.copy(
                item = state.item.copy(content = updatedContent),
                itemContent = updatedContent,
            )
        }
    }

    fun updateSecurityType(securityType: SecurityType) {
        itemState.update { state ->
            state.copy(item = state.item.copy(securityType = securityType))
        }
    }

    fun updateTags(tags: List<String>) {
        updateTagsList()

        itemState.update { state ->
            state.copy(item = state.item.copy(tagIds = tags))
        }
    }

    fun updateTagsList() {
        launchScoped {
            itemState.update { state ->
                state.copy(tags = tagsRepository.getTags(vaultsRepository.getVault().id))
            }
        }
    }
}