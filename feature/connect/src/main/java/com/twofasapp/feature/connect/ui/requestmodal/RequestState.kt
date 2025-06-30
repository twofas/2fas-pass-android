/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.connect.ui.requestmodal

import com.twofasapp.core.common.domain.Login

internal sealed interface RequestState {
    sealed interface FullSize : RequestState {
        data class LoginForm(
            val login: Login,
            val onCancel: () -> Unit = {},
            val onSaveClick: (Login) -> Unit = {},
        ) : FullSize
    }

    sealed interface InsideFrame : RequestState {
        data object Loading : InsideFrame
        data object PasswordRequest : InsideFrame
        data object AddLogin : InsideFrame
        data object UpdateLogin : InsideFrame
        data object DeleteLogin : InsideFrame
        data class UpgradePlan(val maxItems: Int) : InsideFrame
        data class Error(
            val title: String,
            val subtitle: String,
            val cta: String,
        ) : InsideFrame
    }
}