/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.autofill.auth

import com.twofasapp.feature.autofill.service.domain.AutofillLogin

internal data class AutofillAuthUiState(
    val autofillLogin: AutofillLogin = AutofillLogin.Empty,
)