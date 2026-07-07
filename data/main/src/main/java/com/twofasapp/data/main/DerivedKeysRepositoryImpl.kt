/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.crypto.AndroidKeyStore
import com.twofasapp.core.common.crypto.decrypt
import com.twofasapp.core.common.crypto.encrypt
import com.twofasapp.core.common.ktx.decodeHex
import com.twofasapp.core.common.ktx.hmacSha256
import com.twofasapp.data.main.domain.DerivedKeys
import com.twofasapp.data.main.local.DerivedKeysLocalSource
import com.twofasapp.data.main.local.model.DerivedKeysEntity
import kotlinx.coroutines.withContext

internal class DerivedKeysRepositoryImpl(
    private val dispatchers: Dispatchers,
    private val androidKeyStore: AndroidKeyStore,
    private val localDerivedKeys: DerivedKeysLocalSource,
) : DerivedKeysRepository {

    override suspend fun generateAndSaveDerivedKeys(masterKeyHex: String) {
        withContext(dispatchers.io) {
            localDerivedKeys.save(
                DerivedKeysEntity(
                    metadata = encrypt(androidKeyStore.appKey, hmacSha256(masterKeyHex.decodeHex(), "/metadataKey".toByteArray())),
                ),
            )
        }
    }

    override suspend fun getDerivedKeys(): DerivedKeys {
        return withContext(dispatchers.io) {
            val entity = localDerivedKeys.get()

            DerivedKeys(
                metadata = entity.metadata?.let { decrypt(androidKeyStore.appKey, it) },
            )
        }
    }

    override suspend fun clearInMemoryDerivedKeys() {
        withContext(dispatchers.io) {
            localDerivedKeys.clearInMemoryDerivedKeys()
        }
    }
}