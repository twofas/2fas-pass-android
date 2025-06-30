/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.services.webdav.model

import kotlinx.serialization.Serializable

@Serializable
internal class WebDavIndexBackupJson(
    val deviceId: String,
    val deviceName: String,
    val seedHashHex: String,
    val vaultId: String,
    val vaultCreatedAt: Long,
    val vaultUpdatedAt: Long,
    val schemaVersion: Int,
)