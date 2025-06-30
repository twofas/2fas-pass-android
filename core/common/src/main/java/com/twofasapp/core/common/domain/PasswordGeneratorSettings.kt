/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.domain

data class PasswordGeneratorSettings(
    val length: Int = 16,
    val requireDigits: Boolean = true,
    val requireUppercase: Boolean = true,
    val requireSpecial: Boolean = true,
)