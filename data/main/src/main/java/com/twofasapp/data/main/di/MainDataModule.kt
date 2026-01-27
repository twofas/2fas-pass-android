/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.di

import com.twofasapp.core.di.KoinModule
import com.twofasapp.data.main.BackupRepository
import com.twofasapp.data.main.BackupRepositoryImpl
import com.twofasapp.data.main.BrowserExtensionRepository
import com.twofasapp.data.main.BrowserExtensionRepositoryImpl
import com.twofasapp.data.main.CloudRepository
import com.twofasapp.data.main.CloudRepositoryImpl
import com.twofasapp.data.main.ConnectedBrowsersRepository
import com.twofasapp.data.main.ConnectedBrowsersRepositoryImpl
import com.twofasapp.data.main.DeletedItemsRepository
import com.twofasapp.data.main.DeletedItemsRepositoryImpl
import com.twofasapp.data.main.ItemsRepository
import com.twofasapp.data.main.ItemsRepositoryImpl
import com.twofasapp.data.main.SecurityItemRepository
import com.twofasapp.data.main.SecurityItemRepositoryImpl
import com.twofasapp.data.main.SecurityRepository
import com.twofasapp.data.main.SecurityRepositoryImpl
import com.twofasapp.data.main.TagsRepository
import com.twofasapp.data.main.TagsRepositoryImpl
import com.twofasapp.data.main.TrashRepository
import com.twofasapp.data.main.TrashRepositoryImpl
import com.twofasapp.data.main.VaultCipher
import com.twofasapp.data.main.VaultCipherImpl
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.VaultCryptoScopeImpl
import com.twofasapp.data.main.VaultKeysRepository
import com.twofasapp.data.main.VaultKeysRepositoryImpl
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.main.VaultsRepositoryImpl
import com.twofasapp.data.main.domain.CloudMerger
import com.twofasapp.data.main.local.ConnectedBrowsersLocalSource
import com.twofasapp.data.main.local.DeletedItemsLocalSource
import com.twofasapp.data.main.local.ItemsLocalSource
import com.twofasapp.data.main.local.SecurityLocalSource
import com.twofasapp.data.main.local.TagsLocalSource
import com.twofasapp.data.main.local.VaultKeysLocalSource
import com.twofasapp.data.main.local.VaultsLocalSource
import com.twofasapp.data.main.mapper.CloudMapper
import com.twofasapp.data.main.mapper.ConnectedBrowserMapper
import com.twofasapp.data.main.mapper.DeletedItemsMapper
import com.twofasapp.data.main.mapper.IconTypeMapper
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import com.twofasapp.data.main.mapper.ItemMapper
import com.twofasapp.data.main.mapper.ItemSecurityTypeMapper
import com.twofasapp.data.main.mapper.ItemUriMapper
import com.twofasapp.data.main.mapper.SecurityItemMapper
import com.twofasapp.data.main.mapper.TagMapper
import com.twofasapp.data.main.mapper.UnknownItemEncryptionMapper
import com.twofasapp.data.main.mapper.UriMatcherMapper
import com.twofasapp.data.main.mapper.VaultBackupMapper
import com.twofasapp.data.main.mapper.VaultDataForBrowserMapper
import com.twofasapp.data.main.mapper.VaultMapper
import com.twofasapp.data.main.remote.BrowserRequestsRemoteSource
import com.twofasapp.data.main.websocket.ConnectWebSocket
import com.twofasapp.data.main.websocket.ConnectWebSocketImpl
import com.twofasapp.data.main.websocket.RequestWebSocket
import com.twofasapp.data.main.websocket.RequestWebSocketImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

class MainDataModule : KoinModule {
    override fun provide(): Module = module {
        singleOf(::VaultMapper)
        singleOf(::ItemMapper)
        singleOf(::ItemEncryptionMapper)
        singleOf(::UnknownItemEncryptionMapper)
        singleOf(::CloudMapper)
        singleOf(::DeletedItemsMapper)
        singleOf(::ConnectedBrowserMapper)
        singleOf(::IconTypeMapper)
        singleOf(::ItemSecurityTypeMapper)
        singleOf(::ItemUriMapper)
        singleOf(::UriMatcherMapper)
        singleOf(::TagMapper)
        singleOf(::VaultBackupMapper)
        singleOf(::VaultDataForBrowserMapper)
        singleOf(::SecurityItemMapper)

        singleOf(::ItemsLocalSource)
        singleOf(::ItemsRepositoryImpl) { bind<ItemsRepository>() }

        singleOf(::VaultsLocalSource)
        singleOf(::VaultsRepositoryImpl) { bind<VaultsRepository>() }

        singleOf(::VaultKeysLocalSource)
        singleOf(::VaultKeysRepositoryImpl) { bind<VaultKeysRepository>() }

        singleOf(::DeletedItemsLocalSource)
        singleOf(::DeletedItemsRepositoryImpl) { bind<DeletedItemsRepository>() }

        singleOf(::ConnectedBrowsersLocalSource)
        singleOf(::BrowserRequestsRemoteSource)
        singleOf(::BrowserExtensionRepositoryImpl) { bind<BrowserExtensionRepository>() }
        singleOf(::ConnectedBrowsersRepositoryImpl) { bind<ConnectedBrowsersRepository>() }
        factoryOf(::ConnectWebSocketImpl) { bind<ConnectWebSocket>() }
        factoryOf(::RequestWebSocketImpl) { bind<RequestWebSocket>() }

        singleOf(::TrashRepositoryImpl) { bind<TrashRepository>() }

        singleOf(::BackupRepositoryImpl) { bind<BackupRepository>() }

        singleOf(::CloudMerger)
        singleOf(::CloudRepositoryImpl) { bind<CloudRepository>() }

        singleOf(::SecurityLocalSource)
        singleOf(::SecurityRepositoryImpl) { bind<SecurityRepository>() }

        singleOf(::VaultCipherImpl) { bind<VaultCipher>() }
        singleOf(::VaultCryptoScopeImpl) { bind<VaultCryptoScope>() }

        singleOf(::TagsLocalSource)
        singleOf(::TagsRepositoryImpl) { bind<TagsRepository>() }

        singleOf(::SecurityItemRepositoryImpl) { bind<SecurityItemRepository>() }
    }
}