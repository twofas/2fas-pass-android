/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.pluto.plugins.datastore.pref.PlutoDatastoreWatcher
import com.pluto.plugins.rooms.db.PlutoRoomsDBWatcher
import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.core.common.storage.DataStoreOwner
import com.twofasapp.core.di.KoinModule
import com.twofasapp.data.main.local.dao.ConnectedBrowsersDao
import com.twofasapp.data.main.local.dao.DeletedItemsDao
import com.twofasapp.data.main.local.dao.ItemsDao
import com.twofasapp.data.main.local.dao.LoginsDao
import com.twofasapp.data.main.local.dao.TagsDao
import com.twofasapp.data.main.local.dao.VaultKeysDao
import com.twofasapp.data.main.local.dao.VaultsDao
import com.twofasapp.pass.storage.AppDatabase
import com.twofasapp.pass.storage.DataStoreOwnerImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import timber.log.Timber

class StorageModule : KoinModule {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app-datastore")

    override fun provide(): Module = module {
        single<DataStore<Preferences>> {
            androidContext().dataStore.also {
                PlutoDatastoreWatcher.watch("app-datastore", it)
            }
        }
        singleOf(::DataStoreOwnerImpl) { bind<DataStoreOwner>() }

        single<AppDatabase> {
            Room.databaseBuilder(
                androidContext(),
                AppDatabase::class.java,
                "app-db",
            )
                .apply {
                    if (get<AppBuild>().debuggable) {
                        setQueryCallback(
                            { sqlQuery, _ ->
                                if (sqlQuery.startsWith("SELECT") || sqlQuery.startsWith("INSERT")) {
                                    if (sqlQuery.contains("logins") ||
                                        sqlQuery.contains("vaults") ||
                                        sqlQuery.contains("vault_keys") ||
                                        sqlQuery.contains("deleted_logins") ||
                                        sqlQuery.contains("connected_browsers")
                                    ) {
                                        Timber.tag("RoomDatabase").d(sqlQuery)
                                    }
                                }
                            },
                            Dispatchers.IO.asExecutor(),
                        )
                    }
                }
                .build().also {
                    PlutoRoomsDBWatcher.watch("app-db", AppDatabase::class.java)
                }
        }

        single<VaultsDao> { get<AppDatabase>().vaultsDao() }
        single<VaultKeysDao> { get<AppDatabase>().vaultKeysDao() }
        single<ItemsDao> { get<AppDatabase>().itemsDao() }
        single<LoginsDao> { get<AppDatabase>().loginsDao() }
        single<DeletedItemsDao> { get<AppDatabase>().deletedItemsDao() }
        single<ConnectedBrowsersDao> { get<AppDatabase>().connectedBrowsersDao() }
        single<TagsDao> { get<AppDatabase>().tagsDao() }
    }
}