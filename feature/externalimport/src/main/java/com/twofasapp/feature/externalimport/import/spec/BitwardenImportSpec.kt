/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.externalimport.import.spec

import android.content.Context
import android.net.Uri
import com.twofasapp.core.common.domain.IconType
import com.twofasapp.core.common.domain.ImportType
import com.twofasapp.core.common.domain.ItemUri
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.UriMatcher
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.common.ktx.readTextFile
import com.twofasapp.core.locale.R
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.feature.externalimport.import.ImportContent
import com.twofasapp.feature.externalimport.import.ImportSpec
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class BitwardenImportSpec(
    private val vaultsRepository: VaultsRepository,
    private val context: Context,
    private val json: Json,
) : ImportSpec {
    override val type = ImportType.Bitwarden
    override val name = "Bitwarden"
    override val image = com.twofasapp.core.design.R.drawable.external_logo_bitwarden
    override val instructions = context.getString(com.twofasapp.core.locale.R.string.transfer_instructions_bitwarden)
    override val cta: List<ImportSpec.Cta> = listOf(
        ImportSpec.Cta.Primary(
            text = context.getString(R.string.transfer_instructions_cta_json),
            action = ImportSpec.CtaAction.ChooseFile,
        ),
    )

    override suspend fun readContent(uri: Uri): ImportContent {
        val content = json.decodeFromString<Model>(context.readTextFile(uri))
        val vaultId = vaultsRepository.getVault().id
        var skippedCount = 0

        val items = content.items.orEmpty().mapNotNull { item ->
            when (item.type) {
                1 -> Item.create(
                    contentType = ItemContentType.Login,
                    vaultId = vaultId,
                    content = ItemContent.Login.Empty.copy(
                        name = item.name.orEmpty(),
                        username = item.login?.username,
                        password = item.login?.password?.let { SecretField.ClearText(it) },
                        uris = item.login?.uris.orEmpty().map { uri ->
                            ItemUri(
                                text = uri.uri.orEmpty(),
                                matcher = when (uri.match) {
                                    0 -> UriMatcher.Domain
                                    1 -> UriMatcher.Host
                                    2 -> UriMatcher.StartsWith
                                    3 -> UriMatcher.Exact
                                    else -> UriMatcher.Domain
                                },
                            )
                        },
                        iconType = IconType.Icon,
                        iconUriIndex = if (item.login?.uris.isNullOrEmpty()) null else 0,
                        notes = item.notes,
                    ),
                )

                2 -> Item.create(
                    contentType = ItemContentType.SecureNote,
                    vaultId = vaultId,
                    content = ItemContent.SecureNote.Empty.copy(
                        name = item.name.orEmpty(),
                        text = item.notes?.let { SecretField.ClearText(it) },
                    ),
                )

                else -> {
                    skippedCount++
                    null
                }
            }
        }

        return ImportContent(
            items = items,
            tags = emptyList(),
            unknownItems = skippedCount,
        )
    }

    @Serializable
    private data class Model(
        val items: List<BitwardenItem>?,
    )

    @Serializable
    private data class BitwardenItem(
        val type: Int?,
        val name: String?,
        val notes: String?,
        val login: LoginItem?,
    ) {
        @Serializable
        data class LoginItem(
            val username: String?,
            val password: String?,
            val uris: List<Uri>?,
        )

        @Serializable
        data class Uri(
            val uri: String?,
            val match: Int?,
        )
    }
}