/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.domain

import java.time.Instant

sealed interface CloudFileInfo {
    val schemaVersion: Int
    val deviceId: String
    val deviceName: String
    val seedHashHex: String
    val vaultId: String
    val vaultCreatedAt: Instant
    val vaultUpdatedAt: Instant

    data class GoogleDrive(
        val fileId: String,
        override val schemaVersion: Int,
        override val deviceId: String,
        override val deviceName: String,
        override val seedHashHex: String,
        override val vaultId: String,
        override val vaultCreatedAt: Instant,
        override val vaultUpdatedAt: Instant,
    ) : CloudFileInfo

    data class WebDav(
        override val schemaVersion: Int,
        override val deviceId: String,
        override val deviceName: String,
        override val seedHashHex: String,
        override val vaultId: String,
        override val vaultCreatedAt: Instant,
        override val vaultUpdatedAt: Instant,
    ) : CloudFileInfo
}