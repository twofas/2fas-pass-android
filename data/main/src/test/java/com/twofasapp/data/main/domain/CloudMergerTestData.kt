package com.twofasapp.data.main.domain

import com.twofasapp.core.common.domain.DeletedItem
import com.twofasapp.core.common.domain.IconType
import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.Tag
import java.time.Instant

internal fun login(id: String, updatedAt: Long): Login =
    Login(
        id = id,
        vaultId = "vault1",
        name = "test",
        username = "user",
        password = null,
        securityType = SecurityType.Tier3,
        uris = emptyList(),
        iconType = IconType.Icon,
        tagIds = emptyList(),
        updatedAt = updatedAt,
    )

internal fun deletedItem(id: String, deletedAt: Long): DeletedItem =
    DeletedItem(
        id = id,
        vaultId = "vault1",
        type = "login",
        deletedAt = deletedAt,
    )

internal fun vaultBackup(
    logins: List<Login>? = null,
    tags: List<Tag>? = null,
    deletedItems: List<DeletedItem>? = null,
) = VaultBackup(
    schemaVersion = 1,
    originOs = "android",
    originAppVersionCode = 1,
    originAppVersionName = "1.0",
    originDeviceId = "device123",
    originDeviceName = "device",
    vaultId = "vault1",
    vaultName = "Main",
    vaultCreatedAt = 0L,
    vaultUpdatedAt = 0L,
    logins = logins,
    loginsEncrypted = null,
    tags = tags,
    tagsEncrypted = null,
    deletedItems = deletedItems,
    deletedItemsEncrypted = null,
    encryption = null,
)

internal fun tag(id: String, updatedAt: Long): Tag =
    Tag(
        id = id,
        name = "tag-$id",
        vaultId = "vault1",
        position = 0,
        color = null,
        updatedAt = updatedAt,
        assignedItemsCount = 0,
    )

internal fun time(seconds: Long): Long = Instant.ofEpochSecond(seconds).toEpochMilli()