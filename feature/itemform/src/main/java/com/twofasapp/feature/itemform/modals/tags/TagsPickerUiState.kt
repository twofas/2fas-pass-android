/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform.modals.tags

import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item

data class TagsPickerUiState(
    val vaultId: String = "",
    val state: State = State.PickerModal,
    val tags: List<Tag> = emptyList(),
    val initialSelection: Map<Item, Set<String>> = emptyMap(),
    val selection: Map<Item, Set<String>> = emptyMap(),
) {
    val selectedTagIds: List<String>
        get() = selection.values.flatten().distinct()

    val changedSelection: Map<Item, Set<String>> =
        selection.filter { (item, newSet) ->
            val oldSet = initialSelection[item]
            oldSet == null || oldSet != newSet
        }

    enum class State {
        PickerModal, AddTagDialog
    }
}