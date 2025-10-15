/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform.modals.tags

import com.twofasapp.core.common.domain.Tag

data class TagsPickerUiState(
    val vaultId: String = "",
    val state: State = State.PickerModal,
    val tags: List<Tag> = emptyList(),
    val selectedTagIds: List<String> = emptyList(),
) {
    enum class State {
        PickerModal, AddTagDialog
    }
}