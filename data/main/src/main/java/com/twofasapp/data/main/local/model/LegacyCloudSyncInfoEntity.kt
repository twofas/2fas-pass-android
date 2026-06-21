/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.local.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Legacy single-config DataStore entity. Kept for one-time migration into the
 * `cloud_configs` Room table. New configs are stored in Room and this entity
 * should not be written to after migration runs.
 *
 * The shape and the polymorphic discriminators below must match exactly how the
 * legacy `CloudSyncInfoEntity` / `CloudConfigEntity` were persisted (under the
 * `cloudSyncInfo` DataStore key), otherwise deserialization silently falls back
 * to the default empty value and the migration becomes a no-op.
 */
@Serializable
data class LegacyCloudSyncInfoEntity(
    val enabled: Boolean = false,
    val config: ConfigEntity? = null,
    val lastSuccessfulSyncTime: Long = 0,
) {
    @Serializable
    sealed interface ConfigEntity {
        @Serializable
        @SerialName("com.twofasapp.data.main.local.model.CloudConfigEntity.GoogleDrive")
        data class GoogleDrive(
            val id: String,
            val credentialType: String,
        ) : ConfigEntity

        @Serializable
        @SerialName("com.twofasapp.data.main.local.model.CloudConfigEntity.WebDav")
        data class WebDav(
            val username: String,
            val password: String,
            val url: String,
            val allowUntrustedCertificate: Boolean,
        ) : ConfigEntity

        @Serializable
        @SerialName("com.twofasapp.data.main.local.model.CloudConfigEntity.S3")
        data class S3(
            val endpoint: String,
            val region: String,
            val bucket: String,
            val accessKeyId: String,
            val secretAccessKey: String,
            val allowUntrustedCertificate: Boolean,
        ) : ConfigEntity
    }
}