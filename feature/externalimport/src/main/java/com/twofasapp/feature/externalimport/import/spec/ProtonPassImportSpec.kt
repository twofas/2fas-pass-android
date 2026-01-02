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
import com.twofasapp.feature.externalimport.import.CsvParser
import com.twofasapp.feature.externalimport.import.CsvRow
import com.twofasapp.feature.externalimport.import.ImportContent
import com.twofasapp.feature.externalimport.import.ImportSpec
import com.twofasapp.feature.externalimport.import.ZipFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

internal class ProtonPassImportSpec(
    private val vaultsRepository: VaultsRepository,
    private val context: Context,
    private val json: Json,
) : ImportSpec() {
    override val type = ImportType.ProtonPass
    override val name = "Proton Pass"
    override val image = com.twofasapp.core.design.R.drawable.external_logo_protonpass
    override val instructions = context.getString(R.string.transfer_instructions_protonpass)
    override val cta: List<Cta> = listOf(
        Cta.Primary(
            text = context.getString(R.string.transfer_instructions_cta_generic),
            action = CtaAction.ChooseFile,
        ),
    )

    override suspend fun readContent(uri: Uri): ImportContent {
        val vaultId = vaultsRepository.getVault().id

        val zipResult = runCatching {
            ZipFile(uri).read(
                context = context,
                filter = { it.endsWith("data.json") },
            )
        }

        return if (zipResult.isSuccess) {
            val files = zipResult.getOrThrow()
            val jsonText = files.entries.firstOrNull()?.value
            readContentFromJson(jsonText, vaultId)
        } else {
            readContentFromCsv(context.readTextFile(uri), vaultId)
        }
    }

    private fun readContentFromJson(jsonText: String?, vaultId: String): ImportContent {
        var unknownItems = 0

        if (jsonText.isNullOrBlank()) {
            return ImportContent(
                items = emptyList(),
                tags = emptyList(),
                unknownItems = unknownItems,
            )
        }

        val model = json.decodeFromString<ProtonPassExport>(jsonText)

        val items = buildList {
            model.vaults?.values?.forEach { vault ->
                val sourceVaultName = vault.name?.trim()?.takeIf { it.isNotBlank() }

                vault.items?.forEach { item ->
                    // Skip trashed items (state != 1 means not active)
                    if (item.state != 1) return@forEach

                    val itemData = item.data ?: return@forEach
                    val itemType = itemData.type?.lowercase() ?: return@forEach

                    when (itemType) {
                        "login" -> {
                            item.parseLoginFromJson(vaultId, sourceVaultName)?.let { add(it) }
                        }

                        "creditcard" -> {
                            // TODO: Uncomment when payment cards are supported in Android app
                            // item.parseCreditCardFromJson(vaultId, sourceVaultName)?.let { add(it) }
                            // For now, convert to secure note with card details
                            unknownItems++
                            item.parseCreditCardAsSecureNoteFromJson(vaultId, sourceVaultName)?.let { add(it) }
                        }

                        "note" -> {
                            item.parseSecureNoteFromJson(vaultId, sourceVaultName)?.let { add(it) }
                        }

                        else -> {
                            unknownItems++
                            item.parseAsSecureNoteFromJson(vaultId, sourceVaultName)?.let { add(it) }
                        }
                    }
                }
            }
        }

        return ImportContent(
            items = items,
            tags = emptyList(),
            unknownItems = unknownItems,
        )
    }

    private fun ProtonPassItem.parseLoginFromJson(vaultId: String, sourceVaultName: String?): Item? {
        val itemData = data ?: return null
        val metadata = itemData.metadata ?: return null
        val itemName = metadata.name?.trim()?.takeIf { it.isNotBlank() }
        val content = itemData.content ?: return null

        val itemUsername = content["itemUsername"]?.jsonPrimitive?.contentOrNull?.trim()?.takeIf { it.isNotBlank() }
        val itemEmail = content["itemEmail"]?.jsonPrimitive?.contentOrNull?.trim()?.takeIf { it.isNotBlank() }
        val username = itemUsername ?: itemEmail
        val password = content["password"]?.jsonPrimitive?.contentOrNull?.trim()?.takeIf { it.isNotBlank() }

        val uris: List<ItemUri>? = content["urls"]?.jsonArray?.mapNotNull { urlElement ->
            urlElement.jsonPrimitive.contentOrNull?.trim()?.takeIf { it.isNotBlank() }?.let {
                ItemUri(text = it, matcher = UriMatcher.Domain)
            }
        }?.takeIf { it.isNotEmpty() }

        // Build notes
        val noteComponents = mutableListOf<String>()
        metadata.note?.trim()?.takeIf { it.isNotBlank() }?.let { noteComponents.add(it) }

        // Add extra fields from content (excluding used keys)
        val usedKeys = setOf("itemEmail", "itemUsername", "password", "urls", "passkeys", "totpUri")
        val extraKeys = if (itemUsername != null) usedKeys + "itemEmail" else usedKeys
        val additionalData = content.entries
            .filter { !extraKeys.contains(it.key) }
            .mapNotNull { (key, value) ->
                val valueStr = value.toStringOrNull() ?: return@mapNotNull null
                formatFieldType(key) to valueStr
            }
        formatAdditionalFields(additionalData)?.let { noteComponents.add(it) }

        // Add extra fields
        formatExtraFields(itemData.extraFields)?.let { noteComponents.add(it) }

        // Add TOTP if present
        content["totpUri"]?.jsonPrimitive?.contentOrNull?.trim()?.takeIf { it.isNotBlank() }?.let {
            noteComponents.add("TOTP: $it")
        }

        sourceVaultName?.let { noteComponents.add("Vault: $it") }

        val notes = noteComponents.joinToString("\n\n").takeIf { it.isNotBlank() }

        return Item.create(
            contentType = ItemContentType.Login,
            vaultId = vaultId,
            content = ItemContent.Login.Empty.copy(
                name = itemName.orEmpty(),
                username = username,
                password = password?.let { SecretField.ClearText(it) },
                notes = notes,
                iconType = IconType.Icon,
                iconUriIndex = if (uris == null) null else 0,
                uris = uris.orEmpty(),
            ),
        )
    }

    // TODO: When payment cards are supported in Android app, uncomment this method
    // private fun ProtonPassItem.parseCreditCardFromJson(vaultId: String, sourceVaultName: String?): Item? {
    //     val itemName = data.metadata.name.trim().takeIf { it.isNotBlank() }
    //     val content = data.content ?: return null
    //
    //     val cardHolder = content["cardholderName"]?.jsonPrimitive?.contentOrNull?.trim()?.takeIf { it.isNotBlank() }
    //     val cardNumberString = content["number"]?.jsonPrimitive?.contentOrNull?.trim()?.takeIf { it.isNotBlank() }
    //     val securityCodeString = content["verificationNumber"]?.jsonPrimitive?.contentOrNull?.trim()?.takeIf { it.isNotBlank() }
    //
    //     // Parse expiration date from "YYYY-MM" format to "MM/YY"
    //     val expirationDateString: String? = content["expirationDate"]?.jsonPrimitive?.contentOrNull?.trim()?.takeIf { it.isNotBlank() }?.let { expDate ->
    //         val parts = expDate.split("-")
    //         if (parts.size == 2) {
    //             val year = parts[0].takeLast(2)
    //             val month = parts[1]
    //             "$month/$year"
    //         } else {
    //             expDate
    //         }
    //     }
    //
    //     val cardNumber = cardNumberString?.let { SecretField.ClearText(it) }
    //     val expirationDate = expirationDateString?.let { SecretField.ClearText(it) }
    //     val securityCode = securityCodeString?.let { SecretField.ClearText(it) }
    //     val cardNumberMask = cardNumberString?.let { detectCardNumberMask(it) }
    //     val cardIssuer = cardNumberString?.let { detectCardIssuer(it) }
    //
    //     // Build notes
    //     val noteComponents = mutableListOf<String>()
    //     data.metadata.note.trim().takeIf { it.isNotBlank() }?.let { noteComponents.add(it) }
    //
    //     // Add unknown content fields
    //     val usedKeys = setOf("cardholderName", "number", "verificationNumber", "expirationDate", "cardType", "pin")
    //     val additionalData = content.entries
    //         .filter { !usedKeys.contains(it.key) }
    //         .mapNotNull { (key, value) ->
    //             val valueStr = value.jsonPrimitive.contentOrNull?.trim()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
    //             formatFieldType(key) to valueStr
    //         }
    //     formatAdditionalFields(additionalData)?.let { noteComponents.add(it) }
    //
    //     // Add extra fields
    //     formatExtraFields(data.extraFields)?.let { noteComponents.add(it) }
    //
    //     // Add PIN if present
    //     content["pin"]?.jsonPrimitive?.contentOrNull?.trim()?.takeIf { it.isNotBlank() }?.let {
    //         noteComponents.add("PIN: $it")
    //     }
    //
    //     sourceVaultName?.let { noteComponents.add("Vault: $it") }
    //
    //     val notes = noteComponents.joinToString("\n\n").takeIf { it.isNotBlank() }
    //
    //     return Item.create(
    //         contentType = ItemContentType.PaymentCard,
    //         vaultId = vaultId,
    //         content = ItemContent.PaymentCard.Empty.copy(
    //             name = itemName.orEmpty(),
    //             cardHolder = cardHolder,
    //             cardIssuer = cardIssuer,
    //             cardNumber = cardNumber,
    //             cardNumberMask = cardNumberMask,
    //             expirationDate = expirationDate,
    //             securityCode = securityCode,
    //             notes = notes,
    //         ),
    //     )
    // }

    private fun ProtonPassItem.parseCreditCardAsSecureNoteFromJson(vaultId: String, sourceVaultName: String?): Item? {
        val itemData = data ?: return null
        val metadata = itemData.metadata ?: return null
        val itemName = metadata.name?.trim()?.takeIf { it.isNotBlank() }
        val content = itemData.content ?: return null

        val cardHolder = content["cardholderName"]?.jsonPrimitive?.contentOrNull?.trim()?.takeIf { it.isNotBlank() }
        val cardNumberString = content["number"]?.jsonPrimitive?.contentOrNull?.trim()?.takeIf { it.isNotBlank() }
        val securityCodeString = content["verificationNumber"]?.jsonPrimitive?.contentOrNull?.trim()?.takeIf { it.isNotBlank() }
        val pinString = content["pin"]?.jsonPrimitive?.contentOrNull?.trim()?.takeIf { it.isNotBlank() }

        // Parse expiration date from "YYYY-MM" format to "MM/YY"
        val expirationDateString: String? = content["expirationDate"]?.jsonPrimitive?.contentOrNull?.trim()?.takeIf { it.isNotBlank() }?.let { expDate ->
            val parts = expDate.split("-")
            if (parts.size == 2) {
                val year = parts[0].takeLast(2)
                val month = parts[1]
                "$month/$year"
            } else {
                expDate
            }
        }

        // Format card details
        val cardDetails = buildList {
            cardHolder?.let { add("Cardholder: $it") }
            cardNumberString?.let { add("Card Number: $it") }
            expirationDateString?.let { add("Expiration Date: $it") }
            securityCodeString?.let { add("Security Code: $it") }
            pinString?.let { add("PIN: $it") }

            // Add other fields
            val usedKeys = setOf("cardholderName", "number", "verificationNumber", "expirationDate", "cardType", "pin")
            content.entries
                .filter { !usedKeys.contains(it.key) }
                .forEach { (key, value) ->
                    value.toStringOrNull()?.let { valueStr ->
                        add("${formatFieldType(key)}: $valueStr")
                    }
                }
        }.joinToString("\n")

        // Build notes
        val noteComponents = mutableListOf<String>()
        metadata.note?.trim()?.takeIf { it.isNotBlank() }?.let { noteComponents.add(it) }
        cardDetails.takeIf { it.isNotBlank() }?.let { noteComponents.add(it) }
        formatExtraFields(itemData.extraFields)?.let { noteComponents.add(it) }
        sourceVaultName?.let { noteComponents.add("Vault: $it") }

        val displayName = if (itemName != null) {
            "$itemName (Payment Card)"
        } else {
            "(Payment Card)"
        }

        val text = noteComponents.joinToString("\n\n").takeIf { it.isNotBlank() }?.let { SecretField.ClearText(it) }

        return Item.create(
            contentType = ItemContentType.SecureNote,
            vaultId = vaultId,
            content = ItemContent.SecureNote(
                name = displayName,
                text = text,
            ),
        )
    }

    private fun ProtonPassItem.parseSecureNoteFromJson(vaultId: String, sourceVaultName: String?): Item? {
        val itemData = data ?: return null
        val metadata = itemData.metadata ?: return null
        val itemName = metadata.name?.trim()?.takeIf { it.isNotBlank() }

        // Build additional info components
        val additionalInfoComponents = mutableListOf<String>()

        // Add content fields
        itemData.content?.let { content ->
            val contentFields = content.entries.mapNotNull { (key, value) ->
                val valueStr = value.toStringOrNull() ?: return@mapNotNull null
                formatFieldType(key) to valueStr
            }
            formatAdditionalFields(contentFields)?.let { additionalInfoComponents.add(it) }
        }

        // Add extra fields
        formatExtraFields(itemData.extraFields)?.let { additionalInfoComponents.add(it) }

        sourceVaultName?.let { additionalInfoComponents.add("Vault: $it") }

        // Combine note text with additional info
        val noteText = metadata.note?.trim()?.takeIf { it.isNotBlank() }
        val additionalInfo = additionalInfoComponents.joinToString("\n\n").takeIf { it.isNotBlank() }

        val fullText = when {
            noteText != null && additionalInfo != null -> "$noteText\n\n$additionalInfo"
            noteText != null -> noteText
            additionalInfo != null -> additionalInfo
            else -> null
        }

        val text = fullText?.let { SecretField.ClearText(it) }

        return Item.create(
            contentType = ItemContentType.SecureNote,
            vaultId = vaultId,
            content = ItemContent.SecureNote(
                name = itemName.orEmpty(),
                text = text,
            ),
        )
    }

    private fun ProtonPassItem.parseAsSecureNoteFromJson(vaultId: String, sourceVaultName: String?): Item? {
        val itemData = data ?: return null
        val metadata = itemData.metadata ?: return null
        val typeName = formatTypeName(itemData.type ?: "unknown")
        val itemName = metadata.name?.trim()?.takeIf { it.isNotBlank() }

        val displayName = if (itemName != null) {
            "$itemName ($typeName)"
        } else {
            "($typeName)"
        }

        val noteComponents = mutableListOf<String>()

        // Add content fields
        itemData.content?.let { content ->
            val contentFields = content.entries.mapNotNull { (key, value) ->
                val valueStr = value.toStringOrNull() ?: return@mapNotNull null
                formatFieldType(key) to valueStr
            }
            formatAdditionalFields(contentFields)?.let { noteComponents.add(it) }
        }

        // Add extra fields
        formatExtraFields(itemData.extraFields)?.let { noteComponents.add(it) }

        // Add metadata note
        metadata.note?.trim()?.takeIf { it.isNotBlank() }?.let { noteComponents.add(it) }

        sourceVaultName?.let { noteComponents.add("Vault: $it") }

        val text = noteComponents.joinToString("\n\n").takeIf { it.isNotBlank() }?.let { SecretField.ClearText(it) }

        return Item.create(
            contentType = ItemContentType.SecureNote,
            vaultId = vaultId,
            content = ItemContent.SecureNote(
                name = displayName,
                text = text,
            ),
        )
    }

    private fun readContentFromCsv(csvText: String, vaultId: String): ImportContent {
        var unknownItems = 0

        val items = buildList {
            CsvParser.parse(text = csvText) { row ->
                val itemType = row.get("type")?.trim()?.lowercase() ?: "login"

                when (itemType) {
                    "login" -> {
                        parseLoginFromCsv(row, vaultId)?.let { add(it) }
                    }

                    "creditcard" -> {
                        // TODO: Uncomment when payment cards are supported in Android app
                        // parseCreditCardFromCsv(row, vaultId)?.let { add(it) }
                        // For now, convert to secure note with card details
                        unknownItems++
                        parseCreditCardAsSecureNoteFromCsv(row, vaultId)?.let { add(it) }
                    }

                    "note" -> {
                        parseSecureNoteFromCsv(row, vaultId)?.let { add(it) }
                    }

                    else -> {
                        // identity, custom, sshKey, wifi -> convert to secure note
                        unknownItems++
                        parseAsSecureNoteFromCsv(row, itemType, vaultId)?.let { add(it) }
                    }
                }
            }
        }

        return ImportContent(
            items = items,
            tags = emptyList(),
            unknownItems = unknownItems,
        )
    }

    private fun parseLoginFromCsv(row: CsvRow, vaultId: String): Item? {
        val name = row.get("name")?.trim()?.takeIf { it.isNotBlank() }
        val itemUsername = row.get("username")?.trim()?.takeIf { it.isNotBlank() }
        val itemEmail = row.get("email")?.trim()?.takeIf { it.isNotBlank() }
        val username = itemUsername ?: itemEmail
        val password = row.get("password")?.trim()?.takeIf { it.isNotBlank() }

        val uris = row.get("url")?.trim()?.takeIf { it.isNotBlank() }?.let {
            listOf(ItemUri(text = it, matcher = UriMatcher.Domain))
        }

        // Build notes
        val noteComponents = mutableListOf<String>()
        row.get("note")?.trim()?.takeIf { it.isNotBlank() }?.let { noteComponents.add(it) }
        if (itemUsername != null && itemEmail != null) {
            noteComponents.add("Email: $itemEmail")
        }
        row.get("totp")?.trim()?.takeIf { it.isNotBlank() }?.let {
            noteComponents.add("TOTP: $it")
        }
        row.get("vault")?.trim()?.takeIf { it.isNotBlank() }?.let {
            noteComponents.add("Vault: $it")
        }

        val notes = noteComponents.joinToString("\n\n").takeIf { it.isNotBlank() }

        return Item.create(
            contentType = ItemContentType.Login,
            vaultId = vaultId,
            content = ItemContent.Login.Empty.copy(
                name = name.orEmpty(),
                username = username,
                password = password?.let { SecretField.ClearText(it) },
                notes = notes,
                iconType = IconType.Icon,
                iconUriIndex = if (uris == null) null else 0,
                uris = uris.orEmpty(),
            ),
        )
    }

    // TODO: When payment cards are supported in Android app, uncomment this method
    // private fun parseCreditCardFromCsv(row: CsvRow, vaultId: String): Item? {
    //     val name = row.get("name")?.trim()?.takeIf { it.isNotBlank() }
    //
    //     // Credit card data is in the "note" field as JSON
    //     val noteJSON = row.get("note")?.trim()?.takeIf { it.isNotBlank() } ?: return null
    //     val cardData = runCatching {
    //         json.decodeFromString<ProtonPassCSVCreditCard>(noteJSON)
    //     }.getOrNull() ?: return null
    //
    //     val cardHolder = cardData.cardholderName?.trim()?.takeIf { it.isNotBlank() }
    //     val cardNumberString = cardData.number?.trim()?.takeIf { it.isNotBlank() }
    //     val securityCodeString = cardData.verificationNumber?.trim()?.takeIf { it.isNotBlank() }
    //
    //     // Parse expiration date from "YYYY-MM" format to "MM/YY"
    //     val expirationDateString: String? = cardData.expirationDate?.trim()?.takeIf { it.isNotBlank() }?.let { expDate ->
    //         val parts = expDate.split("-")
    //         if (parts.size == 2) {
    //             val year = parts[0].takeLast(2)
    //             val month = parts[1]
    //             "$month/$year"
    //         } else {
    //             expDate
    //         }
    //     }
    //
    //     val cardNumber = cardNumberString?.let { SecretField.ClearText(it) }
    //     val expirationDate = expirationDateString?.let { SecretField.ClearText(it) }
    //     val securityCode = securityCodeString?.let { SecretField.ClearText(it) }
    //     val cardNumberMask = cardNumberString?.let { detectCardNumberMask(it) }
    //     val cardIssuer = cardNumberString?.let { detectCardIssuer(it) }
    //
    //     // Build notes
    //     val noteComponents = mutableListOf<String>()
    //     cardData.note?.trim()?.takeIf { it.isNotBlank() }?.let { noteComponents.add(it) }
    //     cardData.pin?.trim()?.takeIf { it.isNotBlank() }?.let {
    //         noteComponents.add("PIN: $it")
    //     }
    //     row.get("vault")?.trim()?.takeIf { it.isNotBlank() }?.let {
    //         noteComponents.add("Vault: $it")
    //     }
    //
    //     val notes = noteComponents.joinToString("\n\n").takeIf { it.isNotBlank() }
    //
    //     return Item.create(
    //         contentType = ItemContentType.PaymentCard,
    //         vaultId = vaultId,
    //         content = ItemContent.PaymentCard.Empty.copy(
    //             name = name.orEmpty(),
    //             cardHolder = cardHolder,
    //             cardIssuer = cardIssuer,
    //             cardNumber = cardNumber,
    //             cardNumberMask = cardNumberMask,
    //             expirationDate = expirationDate,
    //             securityCode = securityCode,
    //             notes = notes,
    //         ),
    //     )
    // }

    private fun parseCreditCardAsSecureNoteFromCsv(row: CsvRow, vaultId: String): Item? {
        val name = row.get("name")?.trim()?.takeIf { it.isNotBlank() }

        // Credit card data is in the "note" field as JSON
        val noteJSON = row.get("note")?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val cardData = runCatching {
            json.decodeFromString<ProtonPassCSVCreditCard>(noteJSON)
        }.getOrNull() ?: return null

        val cardHolder = cardData.cardholderName?.trim()?.takeIf { it.isNotBlank() }
        val cardNumberString = cardData.number?.trim()?.takeIf { it.isNotBlank() }
        val securityCodeString = cardData.verificationNumber?.trim()?.takeIf { it.isNotBlank() }
        val pinString = cardData.pin?.trim()?.takeIf { it.isNotBlank() }

        // Parse expiration date from "YYYY-MM" format to "MM/YY"
        val expirationDateString: String? = cardData.expirationDate?.trim()?.takeIf { it.isNotBlank() }?.let { expDate ->
            val parts = expDate.split("-")
            if (parts.size == 2) {
                val year = parts[0].takeLast(2)
                val month = parts[1]
                "$month/$year"
            } else {
                expDate
            }
        }

        // Format card details
        val cardDetails = buildList {
            cardHolder?.let { add("Cardholder: $it") }
            cardNumberString?.let { add("Card Number: $it") }
            expirationDateString?.let { add("Expiration Date: $it") }
            securityCodeString?.let { add("Security Code: $it") }
            pinString?.let { add("PIN: $it") }
        }.joinToString("\n")

        val noteComponents = mutableListOf<String>()
        cardData.note?.trim()?.takeIf { it.isNotBlank() }?.let { noteComponents.add(it) }
        cardDetails.takeIf { it.isNotBlank() }?.let { noteComponents.add(it) }
        row.get("vault")?.trim()?.takeIf { it.isNotBlank() }?.let {
            noteComponents.add("Vault: $it")
        }

        val displayName = if (name != null) {
            "$name (Payment Card)"
        } else {
            "(Payment Card)"
        }

        val text = noteComponents.joinToString("\n\n").takeIf { it.isNotBlank() }?.let { SecretField.ClearText(it) }

        return Item.create(
            contentType = ItemContentType.SecureNote,
            vaultId = vaultId,
            content = ItemContent.SecureNote(
                name = displayName,
                text = text,
            ),
        )
    }

    private fun parseSecureNoteFromCsv(row: CsvRow, vaultId: String): Item? {
        val name = row.get("name")?.trim()?.takeIf { it.isNotBlank() }
        val noteContent = row.get("note")?.trim()?.takeIf { it.isNotBlank() }

        val additionalComponents = mutableListOf<String>()
        row.get("vault")?.trim()?.takeIf { it.isNotBlank() }?.let {
            additionalComponents.add("Vault: $it")
        }

        val fullText = when {
            noteContent != null && additionalComponents.isNotEmpty() -> "$noteContent\n\n${additionalComponents.joinToString("\n\n")}"
            noteContent != null -> noteContent
            additionalComponents.isNotEmpty() -> additionalComponents.joinToString("\n\n")
            else -> null
        }

        val text = fullText?.let { SecretField.ClearText(it) }

        return Item.create(
            contentType = ItemContentType.SecureNote,
            vaultId = vaultId,
            content = ItemContent.SecureNote(
                name = name.orEmpty(),
                text = text,
            ),
        )
    }

    private fun parseAsSecureNoteFromCsv(row: CsvRow, itemType: String, vaultId: String): Item? {
        val typeName = formatTypeName(itemType)
        val itemName = row.get("name")?.trim()?.takeIf { it.isNotBlank() }

        val displayName = if (itemName != null) {
            "$itemName ($typeName)"
        } else {
            "($typeName)"
        }

        val noteComponents = mutableListOf<String>()

        // Add fields from CSV (excluding standard keys)
        val excludedKeys = setOf("type", "name", "createTime", "modifyTime", "note", "vault")
        val additionalData = row.map.entries
            .filter { !excludedKeys.contains(it.key) }
            .mapNotNull { (key, value) ->
                val valueStr = value.trim().takeIf { it.isNotBlank() } ?: return@mapNotNull null
                formatFieldType(key) to valueStr
            }
        formatAdditionalFields(additionalData)?.let { noteComponents.add(it) }

        // Try to parse note as JSON for structured data
        row.get("note")?.trim()?.takeIf { it.isNotBlank() }?.let { noteJSON ->
            val jsonData = runCatching {
                json.decodeFromString<JsonObject>(noteJSON)
            }.getOrNull()

            if (jsonData != null) {
                val contentFields = jsonData.entries.mapNotNull { (key, value) ->
                    val valueStr = value.toStringOrNull() ?: return@mapNotNull null
                    formatFieldType(key) to valueStr
                }
                formatAdditionalFields(contentFields)?.let { noteComponents.add(it) }
            } else {
                noteComponents.add(noteJSON)
            }
        }

        row.get("vault")?.trim()?.takeIf { it.isNotBlank() }?.let {
            noteComponents.add("Vault: $it")
        }

        val text = noteComponents.joinToString("\n\n").takeIf { it.isNotBlank() }?.let { SecretField.ClearText(it) }

        return Item.create(
            contentType = ItemContentType.SecureNote,
            vaultId = vaultId,
            content = ItemContent.SecureNote(
                name = displayName,
                text = text,
            ),
        )
    }

    private fun JsonElement.toStringOrNull(): String? {
        return when (this) {
            is JsonPrimitive -> contentOrNull?.trim()?.takeIf { it.isNotBlank() }
            is JsonArray -> toString().trim().takeIf { it.isNotBlank() }
            is JsonObject -> toString().trim().takeIf { it.isNotBlank() }
        }
    }

    private fun formatExtraFields(extraFields: List<ProtonPassExtraField>?): String? {
        if (extraFields.isNullOrEmpty()) return null

        val formatted = extraFields.mapNotNull { field ->
            val fieldName = field.fieldName ?: return@mapNotNull null
            val value: String? = when (field.type) {
                "timestamp" -> field.data?.timestamp?.trim()?.takeIf { it.isNotBlank() }
                else -> field.data?.content?.trim()?.takeIf { it.isNotBlank() }
            }
            value?.let { "$fieldName: $it" }
        }

        return formatted.joinToString("\n").takeIf { it.isNotBlank() }
    }

    private fun formatAdditionalFields(fields: List<Pair<String, String>>): String? {
        if (fields.isEmpty()) return null
        return fields.joinToString("\n") { "${it.first}: ${it.second}" }
    }

    private fun formatFieldType(type: String): String {
        return type
            .replace(Regex("([a-z])([A-Z])"), "$1 $2")
            .replaceFirstChar { it.uppercase() }
    }

    private fun formatTypeName(type: String): String {
        return type.replaceFirstChar { it.uppercase() }
    }

    private fun detectCardNumberMask(cardNumber: String): String? {
        val digitsOnly = cardNumber.filter { it.isDigit() }
        if (digitsOnly.length < 4) return null
        return "**** ${digitsOnly.takeLast(4)}"
    }

    private fun detectCardIssuer(cardNumber: String): ItemContent.PaymentCard.Issuer? {
        val digitsOnly = cardNumber.filter { it.isDigit() }
        if (digitsOnly.isEmpty()) return null

        return when {
            digitsOnly.startsWith("4") -> ItemContent.PaymentCard.Issuer.Visa
            digitsOnly.startsWith("5") -> ItemContent.PaymentCard.Issuer.MasterCard
            digitsOnly.startsWith("34") || digitsOnly.startsWith("37") -> ItemContent.PaymentCard.Issuer.AmericanExpress
            digitsOnly.startsWith("6011") || digitsOnly.startsWith("65") -> ItemContent.PaymentCard.Issuer.Discover
            digitsOnly.startsWith("35") -> ItemContent.PaymentCard.Issuer.Jcb
            digitsOnly.startsWith("62") -> ItemContent.PaymentCard.Issuer.UnionPay
            else -> null
        }
    }

    @Serializable
    private data class ProtonPassExport(
        val vaults: Map<String, ProtonPassVault>? = null,
    )

    @Serializable
    private data class ProtonPassVault(
        val name: String? = null,
        val description: String? = null,
        val items: List<ProtonPassItem>? = null,
    )

    @Serializable
    private data class ProtonPassItem(
        val itemId: String? = null,
        val data: ProtonPassItemData? = null,
        val state: Int? = null,
        val createTime: Int? = null,
        val modifyTime: Int? = null,
    )

    @Serializable
    private data class ProtonPassItemData(
        val metadata: ProtonPassMetadata? = null,
        val extraFields: List<ProtonPassExtraField>? = null,
        val type: String? = null,
        val content: JsonObject? = null,
    )

    @Serializable
    private data class ProtonPassMetadata(
        val name: String? = null,
        val note: String? = null,
    )

    @Serializable
    private data class ProtonPassExtraField(
        val fieldName: String? = null,
        val type: String? = null,
        val data: ProtonPassExtraFieldData? = null,
    )

    @Serializable
    private data class ProtonPassExtraFieldData(
        val content: String? = null,
        val timestamp: String? = null,
    )

    @Serializable
    private data class ProtonPassCSVCreditCard(
        val cardholderName: String? = null,
        val cardType: Int? = null,
        val number: String? = null,
        val verificationNumber: String? = null,
        val expirationDate: String? = null,
        val pin: String? = null,
        val note: String? = null,
    )
}