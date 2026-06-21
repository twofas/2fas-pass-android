/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.storage

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.twofasapp.data.logs.local.dao.LogsDao
import com.twofasapp.data.logs.local.model.LogEntryEntity
import com.twofasapp.data.main.local.dao.CloudConfigsDao
import com.twofasapp.data.main.local.dao.ConnectedBrowsersDao
import com.twofasapp.data.main.local.dao.DeletedItemsDao
import com.twofasapp.data.main.local.dao.ItemsDao
import com.twofasapp.data.main.local.dao.TagsDao
import com.twofasapp.data.main.local.dao.VaultKeysDao
import com.twofasapp.data.main.local.dao.VaultsDao
import com.twofasapp.data.main.local.model.CloudConfigEntity
import com.twofasapp.data.main.local.model.ConnectedBrowserEntity
import com.twofasapp.data.main.local.model.DeletedItemEntity
import com.twofasapp.data.main.local.model.ItemEntity
import com.twofasapp.data.main.local.model.TagEntity
import com.twofasapp.data.main.local.model.VaultEntity
import com.twofasapp.data.main.local.model.VaultKeysEntity
import com.twofasapp.pass.storage.converters.EncryptedBytesConverter
import com.twofasapp.pass.storage.converters.InstantConverter
import com.twofasapp.pass.storage.converters.StringListConverter

@Database(
    entities = [
        LogEntryEntity::class,
        VaultEntity::class,
        VaultKeysEntity::class,
        ItemEntity::class,
        DeletedItemEntity::class,
        ConnectedBrowserEntity::class,
        TagEntity::class,
        CloudConfigEntity::class,
    ],
    version = 4,
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
    ],
)
@TypeConverters(
    InstantConverter::class,
    StringListConverter::class,
    EncryptedBytesConverter::class,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logsDao(): LogsDao
    abstract fun vaultsDao(): VaultsDao
    abstract fun vaultKeysDao(): VaultKeysDao
    abstract fun itemsDao(): ItemsDao
    abstract fun deletedItemsDao(): DeletedItemsDao
    abstract fun connectedBrowsersDao(): ConnectedBrowsersDao
    abstract fun tagsDao(): TagsDao
    abstract fun cloudConfigsDao(): CloudConfigsDao
}