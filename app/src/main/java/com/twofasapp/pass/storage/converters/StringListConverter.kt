/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.storage.converters

import androidx.room.TypeConverter

internal class StringListConverter {

    companion object {
        private const val SEPARATOR = "«§»"
    }

    @TypeConverter
    fun toString(list: List<String>?): String? {
        if (list.isNullOrEmpty()) {
            return null
        }

        return list.joinToString(separator = SEPARATOR)
    }

    @TypeConverter
    fun fromString(text: String?): List<String>? {
        if (text.isNullOrBlank()) {
            return null
        }

        return text.split(SEPARATOR)
    }
}