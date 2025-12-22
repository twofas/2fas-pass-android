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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class KeeperImportSpec(
    private val vaultsRepository: VaultsRepository,
    private val context: Context,
    private val json: Json,
) : ImportSpec {
    override val type = ImportType.Keeper
    override val name = "Keeper"
    override val image = com.twofasapp.core.design.R.drawable.external_logo_keeper
    override val instructions = context.getString(R.string.transfer_instructions_keeper)
    override val cta: List<ImportSpec.Cta> = listOf(
        ImportSpec.Cta.Primary(
            text = context.getString(R.string.transfer_instructions_cta_json),
            action = ImportSpec.CtaAction.ChooseFile,
        ),
    )

    override suspend fun readContent(uri: Uri): ImportContent {
        val content = json.decodeFromString<KeeperModel>(context.readTextFile(uri))
        val vaultId = vaultsRepository.getVault().id

        val items = content.records?.mapNotNull { record ->
            when (record.type) {
                "login" -> importLoginRecord(record, vaultId)
                "encryptedNotes" -> importEncryptedNotesRecord(record, vaultId)
                else -> null
            }
        }.orEmpty()

        return ImportContent(
            items = items,
            tags = emptyList(),
            unknownItems = 0,
        )
    }

    private fun importLoginRecord(record: KeeperRecord, vaultId: String): Item {
        val uris = if (!record.login_url.isNullOrEmpty()) {
            listOf(
                ItemUri(
                    text = record.login_url,
                    matcher = UriMatcher.Domain,
                ),
            )
        } else {
            emptyList()
        }

        return Item.create(
            contentType = ItemContentType.Login,
            vaultId = vaultId,
            content = ItemContent.Login.Empty.copy(
                name = record.title.orEmpty(),
                username = record.login,
                password = record.password?.let { SecretField.ClearText(it) },
                uris = uris,
                iconType = IconType.Icon,
                iconUriIndex = if (uris.isEmpty()) null else 0,
                notes = record.notes,
            ),
        )
    }

    private fun importEncryptedNotesRecord(record: KeeperRecord, vaultId: String): Item {
        // For encryptedNotes, the actual content is in custom_fields.$note::1
        val noteText = record.customFields?.entries
            ?.firstOrNull { it.key.startsWith("\$note::") }
            ?.value
            ?.toString()
            ?: record.notes

        return Item.create(
            contentType = ItemContentType.SecureNote,
            vaultId = vaultId,
            content = ItemContent.SecureNote(
                name = record.title.orEmpty(),
                text = noteText?.let { SecretField.ClearText(it) },
            ),
        )
    }

    @Serializable
    private data class KeeperModel(
        val records: List<KeeperRecord>?,
    )

    @Serializable
    private data class KeeperRecord(
        val uid: Int?,
        val title: String?,
        val notes: String?,
        @SerialName("\$type")
        val type: String? = null,
        val login: String?,
        val password: String?,
        val login_url: String?,
        val custom_fields: Map<String, kotlinx.serialization.json.JsonElement>? = null,
    ) {
        val customFields: Map<String, Any>?
            get() = custom_fields?.mapValues { (_, value) ->
                when {
                    value is kotlinx.serialization.json.JsonPrimitive && value.isString -> value.content
                    value is kotlinx.serialization.json.JsonPrimitive -> value.toString()
                    else -> value.toString()
                }
            }
    }
}