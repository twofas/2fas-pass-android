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

internal class EnpassImportSpec(
    private val vaultsRepository: VaultsRepository,
    private val context: Context,
    private val json: Json,
) : ImportSpec {
    override val type = ImportType.Enpass
    override val name = "Enpass"
    override val image = com.twofasapp.core.design.R.drawable.external_logo_enpass
    override val instructions = context.getString(R.string.transfer_instructions_enpass)
    override val cta: List<ImportSpec.Cta> = listOf(
        ImportSpec.Cta.Primary(
            text = context.getString(R.string.transfer_instructions_cta_json),
            action = ImportSpec.CtaAction.ChooseFile,
        ),
    )

    override suspend fun readContent(uri: Uri): ImportContent {
        val vaultId = vaultsRepository.getVault().id
        val model = json.decodeFromString<Model>(context.readTextFile(uri))

        val items = model.items.orEmpty()
            .filterNot { item -> item.trashed == 1 || item.archived == 1 }
            .mapNotNull { item ->
                when {
                    item.category.isNoteCategory() || item.templateType.isNoteTemplate() -> item.toSecureNote(vaultId)
                    item.templateType.isLoginTemplate() -> item.toLogin(vaultId)
                    else -> null
                }
            }

        return ImportContent(
            items = items,
            skipped = 0,
        )
    }

    @Serializable
    private data class Model(
        val items: List<EnpassItem>? = null,
    )

    @Serializable
    private data class EnpassItem(
        val title: String? = null,
        val subtitle: String? = null,
        val note: String? = null,
        val notes: String? = null,
        val fields: List<EnpassField>? = null,
        val trashed: Int? = null,
        val archived: Int? = null,
        @SerialName("template_type") val templateType: String? = null,
        val category: String? = null,
    )

    @Serializable
    private data class EnpassField(
        val type: String? = null,
        val label: String? = null,
        val value: String? = null,
        val deleted: Int? = null,
    )

    private fun EnpassItem.toLogin(vaultId: String): Item? {
        val fields = fields.orEmpty().filterNot { it.deleted == 1 }

        val username = fields.firstValue(UsernameTypes)
            ?: subtitle?.takeIf { it.isNotBlank() }?.trim()
        val password = fields.firstValue(PasswordTypes)
        val url = fields.firstValue(UrlTypes)?.sanitizeUrl()

        val notesParts = buildList {
            note?.takeIf { it.isNotBlank() }?.let { add(it.trim()) }
            notes?.takeIf { it.isNotBlank() }?.let { add(it.trim()) }
            addAll(fields.values(NoteTypes))
        }

        val notes = notesParts
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(separator = "\n\n")
            .ifBlank { null }

        if (username.isNullOrBlank() && password == null && url == null && notes == null && title.isNullOrBlank()) {
            return null
        }

        val itemUri = url?.let { ItemUri(text = it, matcher = UriMatcher.Domain) }
        val itemName = title?.takeIf { it.isNotBlank() }?.trim()
            ?: url
            ?: username
            ?: notes
            ?: return null

        return Item.create(
            contentType = ItemContentType.Login,
            vaultId = vaultId,
            content = ItemContent.Login.Empty.copy(
                name = itemName,
                username = username,
                password = password?.let { SecretField.ClearText(it) },
                notes = notes,
                iconType = IconType.Icon,
                iconUriIndex = if (itemUri == null) null else 0,
                uris = listOfNotNull(itemUri),
            ),
        )
    }

    private fun EnpassItem.toSecureNote(vaultId: String): Item? {
        val name = title?.takeIf { it.isNotBlank() }?.trim() ?: return null
        val fields = fields.orEmpty().filterNot { it.deleted == 1 }

        val content = buildList {
            note?.takeIf { it.isNotBlank() }?.let { add(it.trim()) }
            notes?.takeIf { it.isNotBlank() }?.let { add(it.trim()) }
            addAll(fields.values(NoteTypes))
        }
            .filter { it.isNotBlank() }
            .joinToString(separator = "\n\n")
            .ifBlank { null } ?: return null

        return Item.create(
            contentType = ItemContentType.SecureNote,
            vaultId = vaultId,
            content = ItemContent.SecureNote(
                name = name,
                text = SecretField.ClearText(content),
            ),
        )
    }

    private fun String.sanitizeUrl(): String {
        val trimmed = trim()
        if (trimmed.isEmpty()) return trimmed
        return if (trimmed.contains("://")) {
            trimmed
        } else {
            "https://${trimmed.removePrefix("//")}"
        }
    }

    private fun List<EnpassField>.firstValue(types: Set<String>): String? = values(types).firstOrNull()

    private fun List<EnpassField>.values(types: Set<String>): List<String> = mapNotNull { field ->
        val fieldType = field.type?.normalizeType()
        if (fieldType != null && fieldType in types) {
            field.value?.takeIf { it.isNotBlank() }?.trim()
        } else {
            null
        }
    }

    private fun String.normalizeTotpSecret(): String = replace(" ", "")
        .replace("-", "")
        .trim()
        .uppercase()

    private fun String?.normalizeType(): String? = this?.lowercase()?.replace("_", "")

    private fun String?.isLoginTemplate(): Boolean = this
        ?.lowercase()
        ?.startsWith("login") == true

    private fun String?.isNoteTemplate(): Boolean = this
        ?.lowercase()
        ?.contains("note") == true

    private fun String?.isNoteCategory(): Boolean = this
        ?.lowercase() == "note"

    private companion object {
        private val UsernameTypes = setOf("username", "email")
        private val PasswordTypes = setOf("password")
        private val UrlTypes = setOf("url", "website")
        private val NoteTypes = setOf("note", "text", "textarea")
    }
}