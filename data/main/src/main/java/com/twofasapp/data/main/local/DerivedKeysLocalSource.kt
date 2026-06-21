/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.local

import com.twofasapp.core.common.logger.Flog
import com.twofasapp.data.main.local.model.DerivedKeysEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class DerivedKeysLocalSource {
    private val inMemory = MutableStateFlow(DerivedKeysEntity(metadata = null))

    fun save(entity: DerivedKeysEntity) {
        inMemory.update { entity }
    }

    fun get(): DerivedKeysEntity {
        return inMemory.value
    }

    suspend fun clearInMemoryDerivedKeys() {
        Flog.tag("DerivedKeys").i("clearInMemoryDerivedKeys")
        inMemory.emit(DerivedKeysEntity(metadata = null))
    }
}