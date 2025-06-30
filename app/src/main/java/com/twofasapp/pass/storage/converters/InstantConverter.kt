/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.storage.converters

import androidx.room.TypeConverter
import java.time.Instant

internal class InstantConverter {

    @TypeConverter
    fun toLong(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }

    @TypeConverter
    fun fromLong(long: Long?): Instant? {
        return long?.let { Instant.ofEpochMilli(it) }
    }
}