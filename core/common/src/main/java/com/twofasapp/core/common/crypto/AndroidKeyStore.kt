/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.crypto

import java.security.Key
import java.security.KeyPair

interface AndroidKeyStore {
    val appKey: Key
    val biometricsKey: Key
    val connectPersistentEcKey: KeyPair

    fun generateConnectEphemeralEcKey(): KeyPair
    fun deleteBiometricsKey()
}