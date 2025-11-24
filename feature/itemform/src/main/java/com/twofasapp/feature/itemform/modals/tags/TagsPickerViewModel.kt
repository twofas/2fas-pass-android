/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform.modals.tags

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.data.main.TagsRepository
import com.twofasapp.data.main.VaultsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class TagsPickerViewModel(
    private val vaultsRepository: VaultsRepository,
    private val tagsRepository: TagsRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(TagsPickerUiState())

    init {
        launchScoped {
            uiState.update { it.copy(vaultId = vaultsRepository.getVault().id) }
        }

        launchScoped {
            tagsRepository.observeTags(vaultsRepository.getVault().id).collect { tags ->
                uiState.update { it.copy(tags = tags) }
            }
        }
    }

    fun init(tags: List<Tag>, items: List<Item>) {
        uiState.update {
            it.copy(
                tags = tags,
                initialSelection = items.associateWith { item -> item.tagIds.toSet() },
                selection = items.associateWith { item -> item.tagIds.toSet() },
            )
        }
    }

    fun openPicker() {
        uiState.update { it.copy(state = TagsPickerUiState.State.PickerModal) }
    }

    fun openAddTag() {
        uiState.update { it.copy(state = TagsPickerUiState.State.AddTagDialog) }
    }

    fun selectTag(tagId: String) {
        uiState.update { state ->
            state.copy(
                selection = state.selection.mapValues { entry ->
                    entry.value.plus(tagId).distinct().toSet()
                },
            )
        }
    }

    fun deselectTag(tagId: String) {
        uiState.update { state ->
            state.copy(
                selection = state.selection.mapValues { entry ->
                    entry.value.minus(tagId).distinct().toSet()
                },
            )
        }
    }

    fun addTag(tag: Tag) {
        launchScoped {
            tagsRepository.saveTags(tag)

            val tags = tagsRepository.getTags(uiState.value.vaultId)

            tags.lastOrNull()?.id?.let { selectTag(it) }
        }
    }
}