/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.domain

import com.twofasapp.core.common.domain.crypto.EncryptedBytes

data class EncryptedLoginUri(
    val text: EncryptedBytes,
    val matcher: LoginUriMatcher = LoginUriMatcher.Domain,
)