/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.storage.internal

import com.twofasapp.core.common.storage.DataStoreOwner
import com.twofasapp.core.common.storage.KeyType
import com.twofasapp.core.common.storage.PrefNullable
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

internal class ReadOnlyPropertyPrefNullable<T>(
    private val keyType: KeyType,
    private val keyName: String?,
    private val encrypted: Boolean,
) : ReadOnlyProperty<DataStoreOwner, PrefNullable<T>> {

    private var cache: PrefNullable<T>? = null

    override fun getValue(thisRef: DataStoreOwner, property: KProperty<*>): PrefNullable<T> {
        return cache ?: PrefNullable<T>(
            owner = thisRef,
            keyType = keyType,
            keyName = keyName ?: property.name,
            encrypted = encrypted,
        ).also { cache = it }
    }
}