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
import com.twofasapp.core.locale.R
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.feature.externalimport.import.ImportContent
import com.twofasapp.feature.externalimport.import.ImportSpec
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal class KeeperImportSpec(
    private val vaultsRepository: VaultsRepository,
    private val context: Context,
    private val json: Json,
) : ImportSpec() {
    override val type = ImportType.Keeper
    override val name = "Keeper"
    override val image = com.twofasapp.core.design.R.drawable.external_logo_keeper
    override val instructions = context.getString(R.string.transfer_instructions_keeper)
    override val additionalInfo = null
    override val cta: List<Cta> = listOf(
        Cta.Primary(
            text = context.getString(R.string.transfer_instructions_cta_json),
            action = CtaAction.ChooseFile,
        ),
    )

    override suspend fun readContent(uri: Uri): ImportContent {
        val vaultId = vaultsRepository.getVault().id
        var unknownItems = 0
        tags.clear()

        val model = json.decodeFromString<Model>(context.readTextFile(uri))

        // Create tags from folders
        val folderToTagId: MutableMap<String, String> = mutableMapOf()

        val items = model.records.orEmpty().mapNotNull { record ->
            // Handle folder -> tag mapping
            val tagIds = record.folders?.firstOrNull()?.folder?.trim()?.takeIf { it.isNotBlank() }?.let { folderName ->
                folderToTagId.getOrPut(folderName) {
                    val tagId = Uuid.generate()
                    tags.add(
                        Tag.create(
                            vaultId = vaultId,
                            id = tagId,
                            name = folderName,
                        ),
                    )
                    tagId
                }?.let { listOf(it) }
            }

            val recordType = record.type ?: "login"

            when (recordType) {
                "login" -> record.parseLogin(vaultId, tagIds)
                "encryptedNotes" -> record.parseSecureNote(vaultId, tagIds)
                "bankCard" -> {
                    // TODO: Uncomment when payment cards are supported in Android app
                    // record.parsePaymentCard(vaultId, tagIds)
                    // For now, convert to secure note with card details
                    unknownItems++
                    record.parsePaymentCardAsSecureNote(vaultId, tagIds)
                }
                else -> {
                    unknownItems++
                    record.parseAsSecureNote(vaultId, tagIds)
                }
            }
        }

        return ImportContent(
            items = items,
            tags = tags,
            unknownItems = unknownItems,
        )
    }

    @Serializable
    private data class Model(
        val records: List<KeeperRecord>? = null,
    )

    @Serializable
    private data class KeeperRecord(
        val uid: Long? = null,
        val title: String? = null,
        val notes: String? = null,
        @SerialName("\$type")
        val type: String? = null,
        val login: String? = null,
        val password: String? = null,
        @SerialName("login_url")
        val loginUrl: String? = null,
        val folders: List<KeeperFolder>? = null,
        @SerialName("custom_fields")
        val customFields: JsonObject? = null,
    )

    @Serializable
    private data class KeeperFolder(
        val folder: String? = null,
    )

    private fun KeeperRecord.parseLogin(vaultId: String, tagIds: List<String>?): Item {
        val itemName = title?.trim()?.takeIf { it.isNotBlank() }
        val noteText = notes?.trim()?.takeIf { it.isNotBlank() }
        val username = login?.trim()?.takeIf { it.isNotBlank() }
        val password = this.password?.trim()?.takeIf { it.isNotBlank() }

        val uris = loginUrl?.trim()?.takeIf { it.isNotBlank() }?.let {
            listOf(ItemUri(text = it, matcher = UriMatcher.Domain))
        }

        val customFieldsInfo = formatCustomFields(customFields)
        val mergedNotes = mergeNote(noteText, customFieldsInfo)

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

    private fun KeeperRecord.parseSecureNote(vaultId: String, tagIds: List<String>?): Item {
        val itemName = title?.trim()?.takeIf { it.isNotBlank() }

        // For encrypted notes, the main content is in custom_fields.$note::1
        val noteContent = customFields?.entries?.firstOrNull { it.key.startsWith("\$note::") }?.let { (_, value) ->
            try {
                (value as? kotlinx.serialization.json.JsonPrimitive)?.content?.trim()?.takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                null
            }
        }
        val recordNotes = notes?.trim()?.takeIf { it.isNotBlank() }

        // Combine the note content with record notes
        val fullNoteText = mergeNote(noteContent, recordNotes)

        // Format remaining custom fields (excluding note)
        val customFieldsInfo = formatCustomFields(customFields, excludeNote = true)

        val text = mergeNote(fullNoteText, customFieldsInfo)?.let { SecretField.ClearText(it) }

        return Item.create(
            contentType = ItemContentType.SecureNote,
            vaultId = vaultId,
            tagIds = tagIds.orEmpty(),
            content = ItemContent.SecureNote(
                name = itemName.orEmpty(),
                text = text,
            ),
        )
    }

    // TODO: When payment cards are supported in Android app, uncomment this method and update readContent
    // private fun KeeperRecord.parsePaymentCard(vaultId: String, tagIds: List<String>?): Item? {
    //     val itemName = title?.trim()?.takeIf { it.isNotBlank() }
    //     val noteText = notes?.trim()?.takeIf { it.isNotBlank() }
    //
    //     // Extract card details from custom_fields
    //     var cardNumberString: String? = null
    //     var expirationDateString: String? = null
    //     var securityCodeString: String? = null
    //     var cardHolder: String? = null
    //     var pinCode: String? = null
    //
    //     customFields?.entries?.forEach { (key, value) ->
    //         when {
    //             key.startsWith("\$paymentCard::") -> {
    //                 // Parse payment card object if it's JSON
    //                 try {
    //                     val cardObj = value as? JsonObject
    //                     cardNumberString = (cardObj?.get("cardNumber") as? kotlinx.serialization.json.JsonPrimitive)?.content
    //                     expirationDateString = (cardObj?.get("cardExpirationDate") as? kotlinx.serialization.json.JsonPrimitive)?.content
    //                     securityCodeString = (cardObj?.get("cardSecurityCode") as? kotlinx.serialization.json.JsonPrimitive)?.content
    //                 } catch (e: Exception) {
    //                     // Ignore parsing errors
    //                 }
    //             }
    //             key.contains(":cardholderName:") -> {
    //                 try {
    //                     cardHolder = (value as? kotlinx.serialization.json.JsonPrimitive)?.content?.trim()?.takeIf { it.isNotBlank() }
    //                 } catch (e: Exception) {
    //                     // Ignore parsing errors
    //                 }
    //             }
    //             key.startsWith("\$pinCode::") -> {
    //                 try {
    //                     pinCode = (value as? kotlinx.serialization.json.JsonPrimitive)?.content?.trim()?.takeIf { it.isNotBlank() }
    //                 } catch (e: Exception) {
    //                     // Ignore parsing errors
    //                 }
    //             }
    //         }
    //     }
    //
    //     val formattedExpirationDate = expirationDateString?.let { formatExpirationDate(it) }
    //
    //     val cardNumber = cardNumberString?.let { SecretField.ClearText(it) }
    //     val expirationDate = formattedExpirationDate?.let { SecretField.ClearText(it) }
    //     val securityCode = securityCodeString?.let { SecretField.ClearText(it) }
    //
    //     val cardNumberMask = cardNumberString?.let { detectCardNumberMask(it) }
    //     val cardIssuer = cardNumberString?.let { detectCardIssuer(it) }
    //
    //     // Add PIN code to notes if present
    //     val additionalInfo = pinCode?.let { "PIN: $it" }
    //     val mergedNotes = mergeNote(noteText, additionalInfo)
    //
    //     return Item.create(
    //         contentType = ItemContentType.PaymentCard,
    //         vaultId = vaultId,
    //         tagIds = tagIds.orEmpty(),
    //         content = ItemContent.PaymentCard.Empty.copy(
    //             name = itemName.orEmpty(),
    //             cardHolder = cardHolder,
    //             cardIssuer = cardIssuer,
    //             cardNumber = cardNumber,
    //             cardNumberMask = cardNumberMask,
    //             expirationDate = expirationDate,
    //             securityCode = securityCode,
    //             notes = mergedNotes,
    //         ),
    //     )
    // }

    // Temporary method to convert payment cards to secure notes
    // TODO: Remove this method when payment cards are supported, and uncomment parsePaymentCard above
    private fun KeeperRecord.parsePaymentCardAsSecureNote(vaultId: String, tagIds: List<String>?): Item {
        val itemName = title?.trim()?.takeIf { it.isNotBlank() }
        val noteText = notes?.trim()?.takeIf { it.isNotBlank() }

        // Extract card details from custom_fields
        var cardNumberString: String? = null
        var expirationDateString: String? = null
        var securityCodeString: String? = null
        var cardHolder: String? = null
        var pinCode: String? = null

        customFields?.entries?.forEach { (key, value) ->
            when {
                key.startsWith("\$paymentCard::") -> {
                    // Parse payment card object if it's JSON
                    try {
                        val cardObj = value as? JsonObject
                        cardNumberString = (cardObj?.get("cardNumber") as? kotlinx.serialization.json.JsonPrimitive)?.content
                        expirationDateString = (cardObj?.get("cardExpirationDate") as? kotlinx.serialization.json.JsonPrimitive)?.content
                        securityCodeString = (cardObj?.get("cardSecurityCode") as? kotlinx.serialization.json.JsonPrimitive)?.content
                    } catch (e: Exception) {
                        // Ignore parsing errors
                    }
                }
                key.contains(":cardholderName:") -> {
                    try {
                        cardHolder = (value as? kotlinx.serialization.json.JsonPrimitive)?.content?.trim()?.takeIf { it.isNotBlank() }
                    } catch (e: Exception) {
                        // Ignore parsing errors
                    }
                }
                key.startsWith("\$pinCode::") -> {
                    try {
                        pinCode = (value as? kotlinx.serialization.json.JsonPrimitive)?.content?.trim()?.takeIf { it.isNotBlank() }
                    } catch (e: Exception) {
                        // Ignore parsing errors
                    }
                }
            }
        }

        val formattedExpirationDate = expirationDateString?.let { formatExpirationDate(it) }

        // Format card details as text for secure note
        val cardDetails = buildList {
            cardHolder?.let { add("Cardholder: $it") }
            cardNumberString?.let {
                val issuer = detectCardIssuer(it)
                if (issuer != null) {
                    add("Card Type: $issuer")
                }
                add("Card Number: $it")
            }
            formattedExpirationDate?.let { add("Expiration Date: $it") }
            securityCodeString?.let { add("Security Code: $it") }
            pinCode?.let { add("PIN: $it") }
        }.joinToString("\n")

        val fullNoteText = mergeNote(noteText, cardDetails.takeIf { it.isNotBlank() })
        val text = fullNoteText?.let { SecretField.ClearText(it) }

        val displayName = if (itemName != null) {
            "$itemName (Payment Card)"
        } else {
            "(Payment Card)"
        }

        return Item.create(
            contentType = ItemContentType.SecureNote,
            vaultId = vaultId,
            tagIds = tagIds.orEmpty(),
            content = ItemContent.SecureNote(
                name = displayName,
                text = text,
            ),
        )
    }

    private fun KeeperRecord.parseAsSecureNote(vaultId: String, tagIds: List<String>?): Item {
        val recordType = type ?: "Unknown"
        val itemName = title?.trim()?.takeIf { it.isNotBlank() }

        val displayName = if (itemName != null) {
            "$itemName (${formatTypeName(recordType)})"
        } else {
            "(${formatTypeName(recordType)})"
        }

        // Gather all data into a single note
        val noteComponents = mutableListOf<String>()

        login?.trim()?.takeIf { it.isNotBlank() }?.let {
            noteComponents.add("Login: $it")
        }
        password?.trim()?.takeIf { it.isNotBlank() }?.let {
            noteComponents.add("Password: $it")
        }
        loginUrl?.trim()?.takeIf { it.isNotBlank() }?.let {
            noteComponents.add("URL: $it")
        }
        formatCustomFields(customFields)?.let {
            noteComponents.add(it)
        }
        notes?.trim()?.takeIf { it.isNotBlank() }?.let {
            noteComponents.add(it)
        }

        val fullNoteText = noteComponents.joinToString("\n").takeIf { it.isNotBlank() }
        val text = fullNoteText?.let { SecretField.ClearText(it) }

        return Item.create(
            contentType = ItemContentType.SecureNote,
            vaultId = vaultId,
            tagIds = tagIds.orEmpty(),
            content = ItemContent.SecureNote(
                name = displayName,
                text = text,
            ),
        )
    }

    private fun formatCustomFields(customFields: JsonObject?, excludeNote: Boolean = false): String? {
        if (customFields == null) return null

        val components = mutableListOf<String>()

        customFields.entries.forEach { (key, value) ->
            when {
                // Skip note if excluded
                excludeNote && key.startsWith("\$note::") -> {}

                // Simple text fields
                key.startsWith("\$text::") || key.startsWith("\$text:") -> {
                    try {
                        val valueStr = (value as? kotlinx.serialization.json.JsonPrimitive)?.content?.trim()?.takeIf { it.isNotBlank() }
                        valueStr?.let { components.add(it) }
                    } catch (e: Exception) {
                        // Ignore parsing errors
                    }
                }

                // Email fields
                key.startsWith("\$email::") -> {
                    try {
                        val valueStr = (value as? kotlinx.serialization.json.JsonPrimitive)?.content?.trim()?.takeIf { it.isNotBlank() }
                        valueStr?.let { components.add("Email: $it") }
                    } catch (e: Exception) {
                        // Ignore parsing errors
                    }
                }

                // URL fields
                key.startsWith("\$url::") -> {
                    try {
                        val valueStr = (value as? kotlinx.serialization.json.JsonPrimitive)?.content?.trim()?.takeIf { it.isNotBlank() }
                        valueStr?.let { components.add("URL: $it") }
                    } catch (e: Exception) {
                        // Ignore parsing errors
                    }
                }

                // License number
                key.startsWith("\$licenseNumber::") -> {
                    try {
                        val valueStr = (value as? kotlinx.serialization.json.JsonPrimitive)?.content?.trim()?.takeIf { it.isNotBlank() }
                        valueStr?.let { components.add("License Number: $it") }
                    } catch (e: Exception) {
                        // Ignore parsing errors
                    }
                }

                // Account numbers
                key.startsWith("\$accountNumber") -> {
                    try {
                        val valueStr = (value as? kotlinx.serialization.json.JsonPrimitive)?.content?.trim()?.takeIf { it.isNotBlank() }
                        valueStr?.let { components.add("Account Number: $it") }
                    } catch (e: Exception) {
                        // Ignore parsing errors
                    }
                }

                // Secret fields
                key.startsWith("\$secret::") -> {
                    try {
                        val valueStr = (value as? kotlinx.serialization.json.JsonPrimitive)?.content?.trim()?.takeIf { it.isNotBlank() }
                        valueStr?.let { components.add("Secret: $it") }
                    } catch (e: Exception) {
                        // Ignore parsing errors
                    }
                }

                // Multiline fields
                key.startsWith("\$multiline::") -> {
                    try {
                        val valueStr = (value as? kotlinx.serialization.json.JsonPrimitive)?.content?.trim()?.takeIf { it.isNotBlank() }
                        valueStr?.let { components.add(it) }
                    } catch (e: Exception) {
                        // Ignore parsing errors
                    }
                }

                // Note field
                key.startsWith("\$note::") -> {
                    try {
                        val valueStr = (value as? kotlinx.serialization.json.JsonPrimitive)?.content?.trim()?.takeIf { it.isNotBlank() }
                        valueStr?.let { components.add("Note: $it") }
                    } catch (e: Exception) {
                        // Ignore parsing errors
                    }
                }

                // Phone fields
                key.startsWith("\$phone::") -> {
                    try {
                        val phoneObj = value as? JsonObject
                        val region = (phoneObj?.get("region") as? kotlinx.serialization.json.JsonPrimitive)?.content
                        val number = (phoneObj?.get("number") as? kotlinx.serialization.json.JsonPrimitive)?.content
                        val ext = (phoneObj?.get("ext") as? kotlinx.serialization.json.JsonPrimitive)?.content

                        val phoneStr = buildString {
                            if (region != null) append("+$region ")
                            if (number != null) append(number)
                            if (ext != null) append(" ext. $ext")
                        }.trim()

                        if (phoneStr.isNotBlank()) {
                            components.add("Phone: $phoneStr")
                        }
                    } catch (e: Exception) {
                        // Ignore parsing errors
                    }
                }

                // Name fields
                key.startsWith("\$name::") || key.contains(":insuredsName:") -> {
                    try {
                        val nameObj = value as? JsonObject
                        val first = (nameObj?.get("first") as? kotlinx.serialization.json.JsonPrimitive)?.content
                        val middle = (nameObj?.get("middle") as? kotlinx.serialization.json.JsonPrimitive)?.content
                        val last = (nameObj?.get("last") as? kotlinx.serialization.json.JsonPrimitive)?.content

                        val fullName = listOfNotNull(first, middle, last)
                            .joinToString(" ")
                            .trim()

                        if (fullName.isNotBlank()) {
                            components.add("Name: $fullName")
                        }
                    } catch (e: Exception) {
                        // Ignore parsing errors
                    }
                }

                // Address fields
                key.startsWith("\$address::") -> {
                    try {
                        val addressObj = value as? JsonObject
                        val street1 = (addressObj?.get("street1") as? kotlinx.serialization.json.JsonPrimitive)?.content
                        val street2 = (addressObj?.get("street2") as? kotlinx.serialization.json.JsonPrimitive)?.content
                        val city = (addressObj?.get("city") as? kotlinx.serialization.json.JsonPrimitive)?.content
                        val state = (addressObj?.get("state") as? kotlinx.serialization.json.JsonPrimitive)?.content
                        val zip = (addressObj?.get("zip") as? kotlinx.serialization.json.JsonPrimitive)?.content
                        val country = (addressObj?.get("country") as? kotlinx.serialization.json.JsonPrimitive)?.content

                        val addressParts = listOfNotNull(street1, street2, city, state, zip, country)
                        if (addressParts.isNotEmpty()) {
                            components.add("Address: ${addressParts.joinToString(", ")}")
                        }
                    } catch (e: Exception) {
                        // Ignore parsing errors
                    }
                }

                // Date fields
                key.startsWith("\$date") || key.startsWith("\$birthDate::") || key.startsWith("\$expirationDate::") -> {
                    try {
                        val timestamp = (value as? kotlinx.serialization.json.JsonPrimitive)?.content?.toLongOrNull()
                        if (timestamp != null) {
                            val date = Date(timestamp)
                            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                            val label = when {
                                key.startsWith("\$birthDate::") -> "Birth Date"
                                key.startsWith("\$expirationDate::") -> "Expiration Date"
                                else -> {
                                    // Extract label from key like "$date:dateActive:1" -> "Date Active"
                                    key.split(":").getOrNull(1)?.let { formatFieldType(it) } ?: "Date"
                                }
                            }
                            components.add("$label: ${dateFormat.format(date)}")
                        }
                    } catch (e: Exception) {
                        // Ignore parsing errors
                    }
                }

                // Plain text custom fields (no $ prefix, not "references")
                !key.startsWith("$") && key != "references" -> {
                    try {
                        val valueStr = (value as? kotlinx.serialization.json.JsonPrimitive)?.content?.trim()?.takeIf { it.isNotBlank() }
                        valueStr?.let { components.add("$key: $it") }
                    } catch (e: Exception) {
                        // Ignore parsing errors
                    }
                }
            }
        }

        return if (components.isEmpty()) null else components.joinToString("\n")
    }

    private fun formatExpirationDate(expDate: String): String {
        // Keeper uses format like "MM/YYYY" or "MMYYYY"
        val cleaned = expDate.replace("/", "").trim()
        if (cleaned.length >= 4) {
            val month = cleaned.substring(0, 2)
            val year = cleaned.substring(2).takeLast(2)
            return "$month/$year"
        }
        return expDate
    }

    private fun formatTypeName(type: String): String {
        return type
            .replace(Regex("([a-z])([A-Z])"), "$1 $2")
            .replaceFirstChar { it.uppercase() }
    }

    private fun formatFieldType(type: String): String {
        return type
            .replace(Regex("([a-z])([A-Z])"), "$1 $2")
            .replaceFirstChar { it.uppercase() }
    }

    private fun mergeNote(note1: String?, note2: String?): String? {
        return when {
            note1 != null && note2 != null -> "$note1\n\n$note2"
            note1 != null -> note1
            note2 != null -> note2
            else -> null
        }
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
}