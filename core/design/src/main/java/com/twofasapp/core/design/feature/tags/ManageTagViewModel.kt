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
import com.twofasapp.core.design.foundation.dialog.InputValidation
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class ManageTagViewModel(
    private val tag: Tag
) : ViewModel() {

    companion object {
        private const val MAX_NAME_LENGTH = 64
    }

    val uiState = MutableStateFlow(
        ManageTagUiState(
            tag = tag,
            colors = TagColor.values().toPersistentList(),
            nameValidation = validateName(
                name = tag.name,
                silent = true
            ),
            colorValidation = validateColor(
                color = tag.color,
                silent = true
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
                nameValidation = validateName(name = trimmedName, silent = false)
            )
        }
    }

    fun onColorSelected(color: TagColor) {
        uiState.update { oldState ->
            oldState.copy(
                tag = oldState.tag.copy(color = color),
                colorValidation = validateColor(color = color, silent = false)
            )
        }
    }

    private fun validateName(name: String, silent: Boolean): InputValidation? {
        if (name.isBlank()) {
            if (silent) {
                return null
            }
            return InputValidation.Invalid("Name can not be empty")
        }
        if (name.length > MAX_NAME_LENGTH) {
            if (silent) {
                return null
            }
            return InputValidation.Invalid("Max length is 64 characters")
        }
        return InputValidation.Valid
    }

    private fun validateColor(color: TagColor?, silent: Boolean): InputValidation? {
        if (color == null) {
            if (silent) {
                return null
            }
            return InputValidation.Invalid(null)
        }
        return InputValidation.Valid
    }
}