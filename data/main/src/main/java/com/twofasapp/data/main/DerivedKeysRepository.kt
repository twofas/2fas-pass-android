/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.data.main.domain.DerivedKeys

interface DerivedKeysRepository {
    suspend fun generateAndSaveDerivedKeys(masterKeyHex: String)
    suspend fun getDerivedKeys(): DerivedKeys
    suspend fun clearInMemoryDerivedKeys()
}