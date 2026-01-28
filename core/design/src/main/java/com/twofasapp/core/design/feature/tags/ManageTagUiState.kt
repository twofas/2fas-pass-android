/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.feature.tags

import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.TagColor
import kotlinx.collections.immutable.ImmutableList

internal data class ManageTagUiState(
    val tag: Tag,
    val colors: ImmutableList<TagColor>,
    val buttonEnabled: Boolean,
    val mode: ManageTagModalMode,
)

internal enum class ManageTagModalMode {
    Add, Edit
}