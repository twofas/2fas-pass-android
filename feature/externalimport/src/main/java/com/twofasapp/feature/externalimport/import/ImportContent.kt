/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.externalimport.import

import com.twofasapp.core.common.domain.Login

internal data class ImportContent(
    val logins: List<Login>,
    val skipped: Int,
)