package com.twofasapp.data.main.domain

import com.twofasapp.core.common.domain.DeletedItem
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import java.time.Instant

internal fun item(id: String, updatedAt: Long): Item =
    Item.Empty.copy(
        id = id,
        vaultId = "vault1",
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
    items: List<Item>? = null,
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
    items = items,
    itemsEncrypted = null,
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