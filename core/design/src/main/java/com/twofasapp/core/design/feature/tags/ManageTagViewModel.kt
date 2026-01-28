/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.feature.tags

import androidx.lifecycle.ViewModel
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.TagColor
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class ManageTagViewModel(
    private val tag: Tag,
    suggestedTagColor: TagColor,
) : ViewModel() {

    companion object {
        private const val MAX_NAME_LENGTH = 64
    }

    val uiState = MutableStateFlow(
        ManageTagUiState(
            tag = tag.copy(color = suggestedTagColor.takeIf { tag.color == null }),
            colors = TagColor.sortedValues().toPersistentList(),
            buttonEnabled = validateName(
                name = tag.name,

                ),
            mode = if (tag.id.isEmpty()) {
                ManageTagModalMode.Add
            } else {
                ManageTagModalMode.Edit
            }
        )
    )

    fun onNameChanged(name: String) {
        val trimmedName = name.take(MAX_NAME_LENGTH)
        uiState.update { oldState ->
            oldState.copy(
                tag = oldState.tag.copy(name = trimmedName),
                buttonEnabled = validateName(name = trimmedName)
            )
        }
    }

    fun onColorSelected(color: TagColor) {
        uiState.update { oldState ->
            oldState.copy(
                tag = oldState.tag.copy(color = color),
            )
        }
    }

    private fun validateName(name: String): Boolean {
        return name.isNotBlank()
    }
}