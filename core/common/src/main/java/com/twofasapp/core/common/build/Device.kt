/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.build

import kotlinx.coroutines.flow.Flow

interface Device {
    suspend fun uniqueId(): String
    suspend fun name(): String
    fun observeName(): Flow<String>
    suspend fun setName(name: String?)
}