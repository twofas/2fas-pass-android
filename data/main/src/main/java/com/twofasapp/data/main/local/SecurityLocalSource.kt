/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.local

import com.twofasapp.core.common.domain.crypto.KdfSpecJson
import com.twofasapp.core.common.storage.DataStoreOwner
import com.twofasapp.core.common.storage.booleanPref
import com.twofasapp.core.common.storage.serializedPrefNullable
import com.twofasapp.core.common.storage.stringPrefNullable

internal class SecurityLocalSource(
    dataStoreOwner: DataStoreOwner,
) : DataStoreOwner by dataStoreOwner {

    /**
     * Reference string is used to quickly verify if the provided user password is correct.
     */
    val encryptionReference by stringPrefNullable()

    /**
     * Master key entropy. It is required for generating access codes.
     */
    val masterKeyEntropy by stringPrefNullable(encrypted = true)

    /**
     * Serialized master key kdf spec. Right now we have only one spec but it may change in future.
     */
    val masterKeyKdfSpec by serializedPrefNullable(serializer = KdfSpecJson.serializer(), encrypted = true)

    /**
     * Master key encrypted with Biometrics key.
     */
    val masterKeyBiometricsEncrypted by stringPrefNullable()

    /**
     * Biometrics enabled.
     */
    val biometricsEnabled by booleanPref(default = false, encrypted = true)
}