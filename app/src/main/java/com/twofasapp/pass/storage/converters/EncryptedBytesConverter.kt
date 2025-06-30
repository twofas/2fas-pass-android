/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.storage.converters

import androidx.room.TypeConverter
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.core.common.ktx.encodeBase64

internal class EncryptedBytesConverter {

    @TypeConverter
    fun toString(value: EncryptedBytes?): String? {
        return value?.bytes?.encodeBase64()
    }

    @TypeConverter
    fun fromString(value: String?): EncryptedBytes? {
        return value?.let { EncryptedBytes(value.decodeBase64()) }
    }
}