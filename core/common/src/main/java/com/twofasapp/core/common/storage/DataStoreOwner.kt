/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.twofasapp.core.common.crypto.AndroidKeyStore
import kotlinx.serialization.json.Json

interface DataStoreOwner {
    val dataStore: DataStore<Preferences>
    val androidKeyStore: AndroidKeyStore
    val json: Json
}