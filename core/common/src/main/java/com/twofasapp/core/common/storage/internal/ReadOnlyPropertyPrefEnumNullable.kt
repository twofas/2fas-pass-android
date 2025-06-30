/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.storage.internal

import com.twofasapp.core.common.storage.DataStoreOwner
import com.twofasapp.core.common.storage.PrefEnumNullable
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

internal class ReadOnlyPropertyPrefEnumNullable<T : Enum<*>>(
    private val keyName: String?,
    private val encrypted: Boolean,
    private val cls: Class<T>,
) : ReadOnlyProperty<DataStoreOwner, PrefEnumNullable<T>> {

    private var cache: PrefEnumNullable<T>? = null

    override fun getValue(thisRef: DataStoreOwner, property: KProperty<*>): PrefEnumNullable<T> {
        return cache ?: PrefEnumNullable(
            owner = thisRef,
            keyName = keyName ?: property.name,
            encrypted = encrypted,
            cls = cls,
        ).also { cache = it }
    }
}