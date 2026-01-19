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
import com.twofasapp.feature.externalimport.import.ImportContent
import com.twofasapp.feature.externalimport.import.ImportSpec
import com.twofasapp.feature.externalimport.import.TransferUtils
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class EnpassImportSpec(
    private val vaultsRepository: VaultsRepository,
    private val context: Context,
    private val json: Json,
) : ImportSpec() {
    override val type = ImportType.Enpass
    override val name = "Enpass"
    override val image = com.twofasapp.core.design.R.drawable.external_logo_enpass
    override val instructions = context.getString(R.string.transfer_instructions_enpass)
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
        model.folders?.forEach { folder ->
            val tagId = Uuid.generate()
            folderToTagId[folder.uuid] = tagId
        }

        // Build parent lookup for folder hierarchy
        val folderParentMap: Map<String, String> = model.folders?.mapNotNull { folder ->
            folder.parentUuid?.takeIf { it.isNotEmpty() }?.let { folder.uuid to it }
        }?.toMap().orEmpty()

        // Create tags with folder names
        model.folders?.forEachIndexed { index, folder ->
            folderToTagId[folder.uuid]?.let { tagId ->
                tags.add(
                    Tag.create(
                        vaultId = vaultId,
                        id = tagId,
                        name = folder.title,
                    ),
                )
            }
        }

        // Helper to get all tag IDs including parent folders
        fun resolveAllTagIds(folderIds: List<String>?): List<String>? {
            if (folderIds.isNullOrEmpty()) return null
            val allTagIds = mutableSetOf<String>()

            folderIds.forEach { folderId ->
                var currentId: String? = folderId
                while (currentId != null) {
                    folderToTagId[currentId]?.let { allTagIds.add(it) }
                    currentId = folderParentMap[currentId]
                }
            }

            return if (allTagIds.isEmpty()) null else allTagIds.toList()
        }

        val items = model.items.orEmpty()
            .filterNot { item -> item.trashed == 1 || item.archived == 1 }
            .mapNotNull { item ->
                val tagIds = resolveAllTagIds(item.folders)

                when (item.category?.lowercase()) {
                    "login", "password" -> item.parseLogin(vaultId, tagIds)
                    "creditcard" -> item.parseCreditCard(vaultId, tagIds)
                    "note" -> item.parseSecureNote(vaultId, tagIds)
                    else -> {
                        // finance, identity, and other categories -> secure note
                        unknownItems++
                        item.parseAsSecureNote(vaultId, tagIds)
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
        val items: List<EnpassItem>? = null,
        val folders: List<EnpassFolder>? = null,
    )

    @Serializable
    private data class EnpassFolder(
        val uuid: String,
        val title: String,
        @SerialName("parent_uuid") val parentUuid: String? = null,
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
        @SerialName("category_name") val categoryName: String? = null,
        val folders: List<String>? = null,
        @SerialName("created_at") val createdAt: Long? = null,
        @SerialName("updated_at") val updatedAt: Long? = null,
    )

    @Serializable
    private data class EnpassField(
        val type: String? = null,
        val label: String? = null,
        val value: String? = null,
        val deleted: Int? = null,
        val sensitive: Int? = null,
    )

    private fun EnpassItem.parseLogin(vaultId: String, tagIds: List<String>?): Item? {
        val name = title?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val noteText = note?.trim()?.takeIf { it.isNotBlank() }

        var username: String? = null
        var email: String? = null
        var password: String? = null
        var urlString: String? = null
        val additionalFields = mutableListOf<Pair<String, String>>()

        fields?.forEach { field ->
            if (field.deleted == 1) return@forEach
            val value = field.value?.trim()?.takeIf { it.isNotBlank() } ?: return@forEach

            when (field.type?.lowercase()) {
                "username" -> {
                    if (username == null) {
                        username = value
                    } else {
                        additionalFields.add((field.label ?: "Username") to value)
                    }
                }

                "email" -> {
                    if (email == null) {
                        email = value
                    } else {
                        additionalFields.add((field.label ?: "E-mail") to value)
                    }
                }

                "password" -> {
                    if (password == null) {
                        password = value
                    } else {
                        additionalFields.add((field.label ?: "Password") to value)
                    }
                }

                "url" -> {
                    if (urlString == null) {
                        urlString = value
                    } else {
                        additionalFields.add((field.label ?: "URL") to value)
                    }
                }

                "section" -> {
                    // Skip section headers
                }

                else -> {
                    val label = field.label ?: formatFieldType(field.type)
                    additionalFields.add(label to value)
                }
            }
        }

        username = username ?: email

        // If both username and email exist, add email to additional fields
        if (username != null && email != null && username != email) {
            additionalFields.add(0, "E-mail" to email)
        }

        val itemUri = urlString?.let { ItemUri(text = it, matcher = UriMatcher.Domain) }

        val mergedNotes = TransferUtils.formatNote(
            note = noteText,
            fields = additionalFields.toMap(),
        )

        return Item.create(
            contentType = ItemContentType.Login,
            vaultId = vaultId,
            tagIds = tagIds.orEmpty(),
            content = ItemContent.Login.Empty.copy(
                name = name,
                username = username,
                password = password?.let { SecretField.ClearText(it) },
                notes = mergedNotes,
                iconType = IconType.Icon,
                iconUriIndex = if (itemUri == null) null else 0,
                uris = listOfNotNull(itemUri),
            ),
        )
    }

    private fun EnpassItem.parseCreditCard(vaultId: String, tagIds: List<String>?): Item {
        val itemName = title?.trim()?.takeIf { it.isNotBlank() }
        val noteText = note?.trim()?.takeIf { it.isNotBlank() }

        var cardHolder: String? = null
        var cardNumberString: String? = null
        var securityCodeString: String? = null
        var pinString: String? = null
        var expirationMonth: String? = null
        var expirationYear: String? = null
        val additionalFields = mutableListOf<Pair<String, String>>()

        fields?.forEach { field ->
            if (field.deleted == 1) return@forEach
            val value = field.value?.trim()?.takeIf { it.isNotBlank() } ?: return@forEach

            when (field.type?.lowercase()) {
                "ccname" -> {
                    if (cardHolder == null) {
                        cardHolder = value
                    } else {
                        additionalFields.add((field.label ?: "Cardholder") to value)
                    }
                }

                "ccnumber" -> {
                    if (cardNumberString == null) {
                        cardNumberString = value
                    } else {
                        additionalFields.add((field.label ?: "Card number") to value)
                    }
                }

                "cccvc" -> {
                    if (securityCodeString == null) {
                        securityCodeString = value
                    } else {
                        additionalFields.add((field.label ?: "CVC") to value)
                    }
                }

                "ccpin" -> {
                    if (pinString == null) {
                        pinString = value
                    } else {
                        additionalFields.add((field.label ?: "PIN") to value)
                    }
                }

                "ccexpiry" -> {
                    // Format is usually "MM/YYYY" or "MM/YY"
                    val parts = value.split("/")
                    if (parts.size == 2) {
                        expirationMonth = parts[0]
                        expirationYear = parts[1]
                    } else {
                        additionalFields.add((field.label ?: "Expiry") to value)
                    }
                }

                "section", "cctype" -> {
                    // Skip section headers and card type
                }

                "ccbankname", "ccvalidfrom", "cctxnpassword" -> {
                    additionalFields.add((field.label ?: formatFieldType(field.type)) to value)
                }

                else -> {
                    val label = field.label ?: formatFieldType(field.type)
                    additionalFields.add(label to value)
                }
            }
        }

        val expirationDateString = if (expirationMonth != null && expirationYear != null) {
            val monthPadded = expirationMonth.padStart(2, '0')
            val yearSuffix = if (expirationYear.length > 2) expirationYear.takeLast(2) else expirationYear.padStart(2, '0')
            "$monthPadded/$yearSuffix"
        } else {
            null
        }

        // Add PIN to additional fields if present
        if (pinString != null) {
            additionalFields.add(0, "PIN" to pinString)
        }

        val cardNumber = cardNumberString?.let { SecretField.ClearText(it.removeWhitespace()) }
        val expirationDate = expirationDateString?.let { SecretField.ClearText(it) }
        val securityCode = securityCodeString?.let { SecretField.ClearText(it) }
        val cardNumberMask = cardNumberString?.let { detectCardNumberMask(it.removeWhitespace()) }
        val cardIssuer = cardNumberString?.let { detectCardIssuer(it) }

        val mergedNotes = TransferUtils.formatNote(
            note = noteText,
            fields = additionalFields.toMap(),
        )

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

    private fun EnpassItem.parseSecureNote(vaultId: String, tagIds: List<String>?): Item? {
        val name = title?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val noteText = note?.trim()?.takeIf { it.isNotBlank() }

        val additionalFields = mutableListOf<Pair<String, String>>()
        fields?.forEach { field ->
            if (field.deleted == 1) return@forEach
            if (field.type == "section") return@forEach
            val value = field.value?.trim()?.takeIf { it.isNotBlank() } ?: return@forEach
            val label = field.label ?: formatFieldType(field.type)
            additionalFields.add(label to value)
        }

        val text = TransferUtils.formatNote(
            note = noteText,
            fields = additionalFields.toMap(),
        )?.let { SecretField.ClearText(it) }

        return Item.create(
            contentType = ItemContentType.SecureNote,
            vaultId = vaultId,
            tagIds = tagIds.orEmpty(),
            content = ItemContent.SecureNote(
                name = name,
                text = text,
                additionalInfo = null,
            ),
        )
    }

    private fun EnpassItem.parseAsSecureNote(vaultId: String, tagIds: List<String>?): Item {
        val categoryName = categoryName ?: formatFieldType(category)
        val itemName = title?.trim()?.takeIf { it.isNotBlank() }

        val name = if (itemName != null) {
            "$itemName ($categoryName)"
        } else {
            "($categoryName)"
        }

        val additionalFields = mutableListOf<Pair<String, String>>()
        fields?.forEach { field ->
            if (field.deleted == 1) return@forEach
            if (field.type == "section") return@forEach
            val value = field.value?.trim()?.takeIf { it.isNotBlank() } ?: return@forEach
            val label = field.label ?: formatFieldType(field.type)
            additionalFields.add(label to value)
        }

        val noteText = note?.trim()?.takeIf { it.isNotBlank() }
        val text = TransferUtils.formatNote(
            note = noteText,
            fields = additionalFields.toMap(),
        )?.let { SecretField.ClearText(it) }

        return Item.create(
            contentType = ItemContentType.SecureNote,
            vaultId = vaultId,
            tagIds = tagIds.orEmpty(),
            content = ItemContent.SecureNote(
                name = name,
                text = text,
                additionalInfo = null,
            ),
        )
    }

    private fun formatFieldType(type: String?): String {
        if (type == null) return "Field"
        return type
            .replace("cc", "Card ", ignoreCase = true)
            .replace("_", " ")
            .replace(Regex("([a-z])([A-Z])"), "$1 $2")
            .replaceFirstChar { it.uppercase() }
    }

    private fun detectCardNumberMask(cardNumber: String): String? {
        val digitsOnly = cardNumber.filter { it.isDigit() }
        if (digitsOnly.length < 4) return null
        return digitsOnly.takeLast(4)
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