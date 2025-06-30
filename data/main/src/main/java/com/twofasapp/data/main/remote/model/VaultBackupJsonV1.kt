/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VaultBackupJsonV1(
    @SerialName("schemaVersion")
    val schemaVersion: Int,
    @SerialName("origin")
    val origin: Origin,
    @SerialName("encryption")
    val encryption: EncryptionSpecJson?,
    @SerialName("vault")
    val vault: Vault,
) {
    @Serializable
    data class Origin(
        @SerialName("os")
        val os: String = "android",
        @SerialName("appVersionCode")
        val appVersionCode: Int,
        @SerialName("appVersionName")
        val appVersionName: String,
        @SerialName("deviceId")
        val deviceId: String?,
        @SerialName("deviceName")
        val deviceName: String,
    )

    @Serializable
    data class Vault(
        @SerialName("id")
        val id: String,
        @SerialName("name")
        val name: String,
        @SerialName("createdAt")
        val createdAt: Long,
        @SerialName("updatedAt")
        val updatedAt: Long,
        @SerialName("logins")
        val logins: List<LoginJson>?,
        @SerialName("loginsEncrypted")
        val loginsEncrypted: List<String>?,
        @SerialName("tags")
        val tags: List<TagJson>?,
        @SerialName("tagsEncrypted")
        val tagsEncrypted: List<String>?,
        @SerialName("itemsDeleted")
        val deletedItems: List<DeletedItemJson>?,
        @SerialName("itemsDeletedEncrypted")
        val deletedItemsEncrypted: List<String>?,
    )
}