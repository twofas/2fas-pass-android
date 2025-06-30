/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.domain

import com.twofasapp.core.common.domain.DeletedItem
import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.common.domain.crypto.EncryptionSpec
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class VaultBackup(
    val schemaVersion: Int,
    val originOs: String,
    val originAppVersionCode: Int,
    val originAppVersionName: String,
    val originDeviceId: String,
    val originDeviceName: String,
    val vaultId: String,
    val vaultName: String,
    val vaultCreatedAt: Long,
    val vaultUpdatedAt: Long,
    val logins: List<Login>?,
    val loginsEncrypted: List<String>?,
    val tags: List<Tag>?,
    val tagsEncrypted: List<String>?,
    val deletedItems: List<DeletedItem>?,
    val deletedItemsEncrypted: List<String>?,
    val encryption: EncryptionSpec?,
) {
    fun generateFilename(): String {
        return "2FAS_Pass_Vault_${vaultId}_${
            Instant.now().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        }.2faspass"
    }

    companion object {
        const val CurrentSchema = 1

        val Empty = VaultBackup(
            schemaVersion = CurrentSchema,
            originOs = "android",
            originAppVersionCode = 0,
            originAppVersionName = "",
            originDeviceId = "",
            originDeviceName = "",
            vaultId = "",
            vaultName = "",
            vaultCreatedAt = 0L,
            vaultUpdatedAt = 0L,
            logins = null,
            loginsEncrypted = null,
            tags = null,
            tagsEncrypted = null,
            deletedItems = null,
            deletedItemsEncrypted = null,
            encryption = null,
        )
    }
}