/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.connect.ui.requestmodal

import com.twofasapp.core.common.domain.items.Item

internal sealed interface RequestState {
    sealed interface FullSize : RequestState {
        data class ItemForm(
            val item: Item,
            val onCancel: () -> Unit = {},
            val onSaveClick: (Item) -> Unit = {},
        ) : FullSize
    }

    sealed interface InsideFrame : RequestState {
        data object Loading : InsideFrame
        data object PasswordRequest : InsideFrame
        data object AddLogin : InsideFrame
        data object UpdateLogin : InsideFrame
        data object FullSync : InsideFrame
        data object SecretFieldRequest : InsideFrame
        data object DeleteItem : InsideFrame
        data object AddItem : InsideFrame
        data class UpgradePlan(val maxItems: Int) : InsideFrame
        data class Error(
            val title: String,
            val subtitle: String,
            val cta: String,
        ) : InsideFrame
    }
}