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
import com.twofasapp.core.common.crypto.Uuid
import com.twofasapp.core.common.domain.IconType
import com.twofasapp.core.common.domain.ImportType
import com.twofasapp.core.common.domain.ItemUri
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.UriMatcher
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.common.ktx.readTextFile
import com.twofasapp.core.common.ktx.removeWhitespace
import com.twofasapp.core.locale.R
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.feature.externalimport.import.CsvParser
import com.twofasapp.feature.externalimport.import.CsvRow
import com.twofasapp.feature.externalimport.import.ImportContent
import com.twofasapp.feature.externalimport.import.ImportSpec
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class BitwardenImportSpec(
    private val vaultsRepository: VaultsRepository,
    private val context: Context,
    private val json: Json,
) : ImportSpec() {
    override val type = ImportType.Bitwarden
    override val name = "Bitwarden"
    override val image = com.twofasapp.core.design.R.drawable.external_logo_bitwarden
    override val instructions = context.getString(R.string.transfer_instructions_bitwarden)
    override val additionalInfo = context.getString(R.string.transfer_instructions_additional_info_bitwarden)
    override val cta: List<Cta> = listOf(
        Cta.Primary(
            text = context.getString(R.string.transfer_instructions_cta_bitwarden),
            action = CtaAction.ChooseFile,
        ),
    )

    override suspend fun readContent(uri: Uri): ImportContent {
        val vaultId = vaultsRepository.getVault().id
        var unknownItems = 0
        tags.clear()

        val fileContent = context.readTextFile(uri)

        // Try JSON parsing first
        val model = try {
            json.decodeFromString<Model>(fileContent)
        } catch (e: Exception) {
            // If JSON parsing fails, try CSV parsing
            return readCsvContent(fileContent, vaultId)
        }

        // Skip encrypted exports
        if (model.encrypted == true) {
            return ImportContent(
                items = emptyList(),
                tags = emptyList(),
                unknownItems = 0,
            )
        }

        // Create tags from folders
        val folderToTagId: MutableMap<String, String> = mutableMapOf()
        model.folders?.forEach { folder ->
            val tagId = Uuid.generate()
            folderToTagId[folder.id] = tagId
        }

        // Create tags with folder names
        model.folders?.forEachIndexed { index, folder ->
            folderToTagId[folder.id]?.let { tagId ->
                tags.add(
                    Tag.create(
                        vaultId = vaultId,
                        id = tagId,
                        name = folder.name,
                    ),
                )
            }
        }

        val items = model.items.orEmpty().mapNotNull { item ->
            val tagIds = item.folderId?.let { folderId ->
                folderToTagId[folderId]?.let { listOf(it) }
            }

            when (item.type) {
                ItemType.LOGIN.value -> item.parseLogin(vaultId, tagIds)
                ItemType.SECURE_NOTE.value -> item.parseSecureNote(vaultId, tagIds)
                ItemType.CARD.value -> {
                    item.parseCard(vaultId, tagIds)
                }

                ItemType.IDENTITY.value -> {
                    unknownItems++
                    item.parseAsSecureNote(vaultId, tagIds, "Identity", item.identity)
                }

                ItemType.SSH_KEY.value -> {
                    unknownItems++
                    item.parseAsSecureNote(vaultId, tagIds, "SSH Key", item.sshKey)
                }

                else -> {
                    unknownItems++
                    null
                }
            }
        }

        return ImportContent(
            items = items,
            tags = tags,
            unknownItems = unknownItems,
        )
    }

    private fun readCsvContent(fileContent: String, vaultId: String): ImportContent {
        var unknownItems = 0
        val items = mutableListOf<Item>()
        val folderToTagId = mutableMapOf<String, String>()

        CsvParser.parse(
            text = fileContent,
            delimiter = ',',
        ) { row ->
            // Skip completely empty rows
            if (row.map.values.all { it.isBlank() }) return@parse

            // Verify this is a Bitwarden CSV by checking for required columns
            if (items.isEmpty() && !row.map.containsKey("type") && !row.map.containsKey("name")) {
                throw IllegalArgumentException("Invalid Bitwarden CSV format")
            }

            // Handle folder -> tag mapping
            val tagIds = row.get("folder")?.takeIf { it.isNotBlank() }?.let { folderName ->
                val tagId = folderToTagId.getOrPut(folderName) {
                    val newTagId = Uuid.generate()
                    tags.add(
                        Tag.create(
                            vaultId = vaultId,
                            id = newTagId,
                            name = folderName,
                        ),
                    )
                    newTagId
                }
                listOf(tagId)
            }

            val itemType = row.get("type") ?: "login"

            when (itemType.lowercase()) {
                "login" -> {
                    parseCsvLogin(row, vaultId, tagIds)?.let { items.add(it) }
                }

                "note" -> {
                    parseCsvSecureNote(row, vaultId, tagIds)?.let { items.add(it) }
                }

                "card" -> {
                    parseCsvCard(row, vaultId, tagIds)?.let { items.add(it) }
                }

                else -> {
                    unknownItems++
                }
            }
        }

        return ImportContent(
            items = items,
            tags = tags,
            unknownItems = unknownItems,
        )
    }

    private fun parseCsvLogin(row: CsvRow, vaultId: String, tagIds: List<String>?): Item? {
        val itemName = row.get("name")?.trim()?.takeIf { it.isNotBlank() }
        val noteText = row.get("notes")?.trim()?.takeIf { it.isNotBlank() }
        val username = row.get("login_username")?.trim()?.takeIf { it.isNotBlank() }
        val password = row.get("login_password")?.trim()?.takeIf { it.isNotBlank() }

        val uris = row.get("login_uri")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.split(Regex(",(?=[a-zA-Z][a-zA-Z0-9+.-]*://)"))
            ?.map { uriString ->
                ItemUri(text = uriString, matcher = UriMatcher.Domain)
            }

        val fieldsInfo = row.get("fields")?.trim()?.takeIf { it.isNotBlank() }
        val mergedNotes = mergeNote(noteText, fieldsInfo)

        return Item.create(
            contentType = ItemContentType.Login,
            vaultId = vaultId,
            tagIds = tagIds.orEmpty(),
            content = ItemContent.Login.Empty.copy(
                name = itemName.orEmpty(),
                username = username,
                password = password?.let { SecretField.ClearText(it) },
                notes = mergedNotes,
                iconType = IconType.Icon,
                iconUriIndex = if (uris == null) null else 0,
                uris = uris.orEmpty(),
            ),
        )
    }

    private fun parseCsvSecureNote(row: CsvRow, vaultId: String, tagIds: List<String>?): Item? {
        val itemName = row.get("name")?.trim()?.takeIf { it.isNotBlank() }
        val noteText = row.get("notes")?.trim()?.takeIf { it.isNotBlank() }
        val fieldsInfo = row.get("fields")?.trim()?.takeIf { it.isNotBlank() }

        val mergedText = mergeNote(noteText, fieldsInfo)
        val text = mergedText?.let { SecretField.ClearText(it) }

        return Item.create(
            contentType = ItemContentType.SecureNote,
            vaultId = vaultId,
            tagIds = tagIds.orEmpty(),
            content = ItemContent.SecureNote(
                name = itemName.orEmpty(),
                text = text,
                additionalInfo = null,
            ),
        )
    }

    private fun parseCsvCard(row: CsvRow, vaultId: String, tagIds: List<String>?): Item? {
        val itemName = row.get("name")?.trim()?.takeIf { it.isNotBlank() }
        val noteText = row.get("notes")?.trim()?.takeIf { it.isNotBlank() }

        val cardHolder = row.get("card_cardholdername")?.trim()?.takeIf { it.isNotBlank() }
        val cardNumberString = row.get("card_number")?.trim()?.takeIf { it.isNotBlank() }?.removeWhitespace()
        val securityCodeString = row.get("card_code")?.trim()?.takeIf { it.isNotBlank() }?.removeWhitespace()
        val brand = row.get("card_brand")?.trim()?.takeIf { it.isNotBlank() }

        val expirationMonth = row.get("card_expmonth")?.trim()?.takeIf { it.isNotBlank() }
        val expirationYear = row.get("card_expyear")?.trim()?.takeIf { it.isNotBlank() }

        val expirationDateString = if (expirationMonth != null && expirationYear != null) {
            val monthPadded = expirationMonth.padStart(2, '0')
            val yearSuffix = if (expirationYear.length > 2) expirationYear.takeLast(2) else expirationYear.padStart(2, '0')
            "$monthPadded/$yearSuffix"
        } else {
            null
        }

        val cardNumber = cardNumberString?.let { SecretField.ClearText(it) }
        val expirationDate = expirationDateString?.let { SecretField.ClearText(it) }
        val securityCode = securityCodeString?.let { SecretField.ClearText(it) }
        val cardNumberMask = cardNumberString?.let { detectCardNumberMask(it) }
        val cardIssuer = cardNumberString?.let { detectCardIssuer(it) } ?: brand?.let { detectCardIssuerFromBrand(it) }

        val fieldsInfo = row.get("fields")?.trim()?.takeIf { it.isNotBlank() }
        val mergedNotes = mergeNote(noteText, fieldsInfo)

        return Item.create(
            contentType = ItemContentType.PaymentCard,
            vaultId = vaultId,
            tagIds = tagIds.orEmpty(),
            content = ItemContent.PaymentCard.Empty.copy(
                name = itemName.orEmpty(),
                cardHolder = cardHolder,
                cardIssuer = cardIssuer,
                cardNumber = cardNumber,
                cardNumberMask = cardNumberMask,
                expirationDate = expirationDate,
                securityCode = securityCode,
                notes = mergedNotes,
            ),
        )
    }

    @Serializable
    private data class Model(
        val encrypted: Boolean? = null,
        val folders: List<BitwardenFolder>? = null,
        val items: List<BitwardenItem>? = null,
    )

    @Serializable
    private data class BitwardenFolder(
        val id: String,
        val name: String,
    )

    @Serializable
    private data class BitwardenItem(
        val name: String? = null,
        val notes: String? = null,
        val type: Int? = null,
        val folderId: String? = null,
        val creationDate: String? = null,
        val revisionDate: String? = null,
        val fields: List<BitwardenField>? = null,
        val login: JsonObject? = null,
        val secureNote: JsonObject? = null,
        val card: JsonObject? = null,
        val identity: JsonObject? = null,
        val sshKey: JsonObject? = null,
    )

    @Serializable
    private data class BitwardenField(
        val name: String? = null,
        val value: String? = null,
        val type: Int? = null,
    )

    private enum class ItemType(val value: Int) {
        LOGIN(1),
        SECURE_NOTE(2),
        CARD(3),
        IDENTITY(4),
        SSH_KEY(5),
        ;

        companion object {
            fun fromValue(value: Int?): ItemType? = entries.find { it.value == value }
        }
    }

    private fun BitwardenItem.parseLogin(vaultId: String, tagIds: List<String>?): Item? {
        val itemName = name?.trim()?.takeIf { it.isNotBlank() }
        val noteText = notes?.trim()?.takeIf { it.isNotBlank() }

        val loginData = login ?: return null
        val username = loginData["username"]?.jsonPrimitive?.content?.trim()?.takeIf { it.isNotBlank() }
        val password = loginData["password"]?.jsonPrimitive?.content?.trim()?.takeIf { it.isNotBlank() }

        val uris: List<ItemUri>? = loginData["uris"]?.let { urisElement ->
            if (urisElement is kotlinx.serialization.json.JsonArray) {
                urisElement.mapNotNull { uriElement ->
                    if (uriElement is JsonObject) {
                        val uriString = uriElement["uri"]?.jsonPrimitive?.content?.trim()?.takeIf { it.isNotBlank() }
                        val matchType = uriElement["match"]?.jsonPrimitive?.content?.toIntOrNull()

                        uriString?.let {
                            ItemUri(
                                text = it,
                                matcher = when (matchType) {
                                    0 -> UriMatcher.Domain
                                    1 -> UriMatcher.Host
                                    2 -> UriMatcher.StartsWith
                                    3 -> UriMatcher.Exact
                                    else -> UriMatcher.Domain
                                },
                            )
                        }
                    } else {
                        null
                    }
                }
            } else {
                null
            }
        }?.takeIf { it.isNotEmpty() }

        // Extract additional login data (excluding used keys)
        val usedKeys = setOf("username", "password", "uris", "fido2Credentials")
        val additionalLoginData = loginData.entries
            .filter { !usedKeys.contains(it.key) }
            .mapNotNull { (key, value) ->
                val valueStr = value.jsonPrimitive.content.trim().takeIf { it.isNotBlank() } ?: return@mapNotNull null
                formatFieldType(key) to valueStr
            }

        val fieldsInfo = formatCustomFields(fields)
        val additionalLoginInfo = formatAdditionalFields(additionalLoginData)
        val additionalInfo = mergeNote(additionalLoginInfo, fieldsInfo)
        val mergedNotes = mergeNote(noteText, additionalInfo)

        return Item.create(
            contentType = ItemContentType.Login,
            vaultId = vaultId,
            tagIds = tagIds.orEmpty(),
            content = ItemContent.Login.Empty.copy(
                name = itemName.orEmpty(),
                username = username,
                password = password?.let { SecretField.ClearText(it) },
                notes = mergedNotes,
                iconType = IconType.Icon,
                iconUriIndex = if (uris == null) null else 0,
                uris = uris.orEmpty(),
            ),
        )
    }

    private fun BitwardenItem.parseSecureNote(vaultId: String, tagIds: List<String>?): Item {
        val itemName = name?.trim()?.takeIf { it.isNotBlank() }
        val noteText = notes?.trim()?.takeIf { it.isNotBlank() }

        // Extract additional secure note data (excluding used keys)
        val secureNoteData = secureNote ?: JsonObject(emptyMap())
        val usedKeys = setOf("type")
        val additionalSecureNoteData = secureNoteData.entries
            .filter { !usedKeys.contains(it.key) }
            .mapNotNull { (key, value) ->
                val valueStr = value.jsonPrimitive.content.trim().takeIf { it.isNotBlank() } ?: return@mapNotNull null
                formatFieldType(key) to valueStr
            }

        val fieldsInfo = formatCustomFields(fields)
        val additionalSecureNoteInfo = formatAdditionalFields(additionalSecureNoteData)
        val additionalInfo = mergeNote(additionalSecureNoteInfo, fieldsInfo)

        val text = mergeNote(noteText, additionalInfo)?.let { SecretField.ClearText(it) }

        return Item.create(
            contentType = ItemContentType.SecureNote,
            vaultId = vaultId,
            tagIds = tagIds.orEmpty(),
            content = ItemContent.SecureNote(
                name = itemName.orEmpty(),
                text = text,
                additionalInfo = null,
            ),
        )
    }

    private fun BitwardenItem.parseCard(vaultId: String, tagIds: List<String>?): Item? {
        val itemName = name?.trim()?.takeIf { it.isNotBlank() }
        val noteText = notes?.trim()?.takeIf { it.isNotBlank() }

        val cardData = card ?: return null
        val cardHolder = cardData["cardholderName"]?.jsonPrimitive?.content?.trim()?.takeIf { it.isNotBlank() }
        val cardNumberString = cardData["number"]?.jsonPrimitive?.content?.trim()?.takeIf { it.isNotBlank() }?.removeWhitespace()
        val securityCodeString = cardData["code"]?.jsonPrimitive?.content?.trim()?.takeIf { it.isNotBlank() }?.removeWhitespace()
        val brand = cardData["brand"]?.jsonPrimitive?.content?.trim()?.takeIf { it.isNotBlank() }

        val expirationMonth = cardData["expMonth"]?.jsonPrimitive?.content?.trim()?.takeIf { it.isNotBlank() }
        val expirationYear = cardData["expYear"]?.jsonPrimitive?.content?.trim()?.takeIf { it.isNotBlank() }

        val expirationDateString = if (expirationMonth != null && expirationYear != null) {
            val monthPadded = expirationMonth.padStart(2, '0')
            val yearSuffix = if (expirationYear.length > 2) expirationYear.takeLast(2) else expirationYear.padStart(2, '0')
            "$monthPadded/$yearSuffix"
        } else {
            null
        }

        // Extract additional card data (excluding used keys)
        val usedKeys = setOf("cardholderName", "number", "code", "expMonth", "expYear", "brand")
        val additionalCardData = cardData.entries
            .filter { !usedKeys.contains(it.key) }
            .mapNotNull { (key, value) ->
                val valueStr = value.jsonPrimitive.content.trim().takeIf { it.isNotBlank() } ?: return@mapNotNull null
                formatFieldType(key) to valueStr
            }

        // Format card details as text for secure note
        val cardDetails = buildList {
            cardHolder?.let { add("Cardholder: $it") }
            cardNumberString?.let { add("Card Number: $it") }
            expirationDateString?.let { add("Expiration Date: $it") }
            securityCodeString?.let { add("Security Code: $it") }
            brand?.let { add("Brand: $it") }
            additionalCardData.forEach { (label, value) ->
                add("$label: $value")
            }
        }.joinToString("\n")

        val fieldsInfo = formatCustomFields(fields)

        val cardNumber = cardNumberString?.let { SecretField.ClearText(it) }
        val expirationDate = expirationDateString?.let { SecretField.ClearText(it) }
        val securityCode = securityCodeString?.let { SecretField.ClearText(it) }
        val cardNumberMask = cardNumberString?.let { detectCardNumberMask(it) }
        val cardIssuer = cardNumberString?.let { detectCardIssuer(it) } ?: brand?.let { detectCardIssuerFromBrand(it) }

        return Item.create(
            contentType = ItemContentType.PaymentCard,
            vaultId = vaultId,
            tagIds = tagIds.orEmpty(),
            content = ItemContent.PaymentCard.Empty.copy(
                name = itemName.orEmpty(),
                cardHolder = cardHolder,
                cardIssuer = cardIssuer,
                cardNumber = cardNumber,
                cardNumberMask = cardNumberMask,
                expirationDate = expirationDate,
                securityCode = securityCode,
                notes = mergeNote(noteText, mergeNote(formatAdditionalFields(additionalCardData), fieldsInfo)),
            ),
        )
    }

    private fun BitwardenItem.parseAsSecureNote(
        vaultId: String,
        tagIds: List<String>?,
        contentTypeName: String,
        data: JsonObject?,
    ): Item {
        val itemName = name?.trim()?.takeIf { it.isNotBlank() }

        val displayName = if (itemName != null) {
            "$itemName ($contentTypeName)"
        } else {
            "($contentTypeName)"
        }

        val additionalData = data?.entries?.mapNotNull { (key, value) ->
            val valueStr = value.jsonPrimitive.content.trim().takeIf { it.isNotBlank() } ?: return@mapNotNull null
            formatFieldType(key) to valueStr
        }.orEmpty()

        val dataInfo = formatAdditionalFields(additionalData)
        val fieldsInfo = formatCustomFields(fields)
        val additionalInfo = mergeNote(dataInfo, fieldsInfo)
        val noteText = mergeNote(additionalInfo, notes?.trim()?.takeIf { it.isNotBlank() })

        val text = noteText?.let { SecretField.ClearText(it) }

        return Item.create(
            contentType = ItemContentType.SecureNote,
            vaultId = vaultId,
            tagIds = tagIds.orEmpty(),
            content = ItemContent.SecureNote(
                name = displayName,
                text = text,
                additionalInfo = null,
            ),
        )
    }

    private fun formatCustomFields(fields: List<BitwardenField>?): String? {
        if (fields.isNullOrEmpty()) return null

        val formatted = fields.mapNotNull { field ->
            // Skip linked fields (type 3)
            if (field.type == 3) return@mapNotNull null
            val name = field.name?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            val value = field.value ?: ""
            "$name: $value"
        }

        return if (formatted.isEmpty()) null else formatted.joinToString("\n")
    }

    private fun formatAdditionalFields(fields: List<Pair<String, String>>): String? {
        if (fields.isEmpty()) return null
        return fields.joinToString("\n") { "${it.first}: ${it.second}" }
    }

    private fun mergeNote(note1: String?, note2: String?): String? {
        return when {
            note1 != null && note2 != null -> "$note1\n\n$note2"
            note1 != null -> note1
            note2 != null -> note2
            else -> null
        }
    }

    private fun formatFieldType(type: String): String {
        return type
            .replace(Regex("([a-z])([A-Z])"), "$1 $2")
            .replaceFirstChar { it.uppercase() }
    }

    private fun detectCardIssuerFromBrand(brand: String): ItemContent.PaymentCard.Issuer? {
        return when (brand.lowercase()) {
            "visa" -> ItemContent.PaymentCard.Issuer.Visa
            "mastercard", "mc" -> ItemContent.PaymentCard.Issuer.MasterCard
            "amex", "american express" -> ItemContent.PaymentCard.Issuer.AmericanExpress
            "discover" -> ItemContent.PaymentCard.Issuer.Discover
            "jcb" -> ItemContent.PaymentCard.Issuer.Jcb
            "unionpay" -> ItemContent.PaymentCard.Issuer.UnionPay
            "diners club", "dinersclub" -> ItemContent.PaymentCard.Issuer.DinersClub
            else -> null
        }
    }
}