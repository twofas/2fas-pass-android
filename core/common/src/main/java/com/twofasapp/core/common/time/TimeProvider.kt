/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.time

import java.time.Instant

interface TimeProvider {
    fun systemElapsedTime(): Long
    fun currentTimeUtc(): Long
    fun currentTimeUtcInstant(): Instant
    suspend fun sync()
}