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
import com.twofasapp.core.common.ktx.removeWhitespace
import com.twofasapp.core.locale.R
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.feature.externalimport.import.CsvParser
import com.twofasapp.feature.externalimport.import.ImportContent
import com.twofasapp.feature.externalimport.import.ImportSpec
import com.twofasapp.feature.externalimport.import.ZipFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class OnePasswordImportSpec(
    private val vaultsRepository: VaultsRepository,
    private val context: Context,
    private val json: Json,
) : ImportSpec() {
    override val type = ImportType.OnePassword
    override val name = "1Password"
    override val image = com.twofasapp.core.design.R.drawable.external_logo_onepassword
    override val instructions = context.getString(R.string.transfer_instructions_onepassword)
    override val additionalInfo = context.getString(R.string.transfer_instructions_additional_info_onepassword)
    override val cta: List<Cta> = listOf(
        Cta.Primary(
            text = context.getString(R.string.transfer_instructions_cta_onepassword),
            action = CtaAction.ChooseFile,
        ),
    )

    override suspend fun readContent(uri: Uri): ImportContent {
        val vaultId = vaultsRepository.getVault().id

        val zipResult = runCatching {
            ZipFile(uri).read(
                context = context,
                filter = { it == "export.data" },
            )
        }

        return if (zipResult.isSuccess) {
            val files = zipResult.getOrThrow()
            val jsonText = files.entries.firstOrNull()?.value
            readContentFrom1Pux(jsonText, vaultId)
        } else {
            readContentFromCsv(context.readTextFile(uri), vaultId)
        }
    }

    private fun readContentFromCsv(csvText: String, vaultId: String): ImportContent {
        tags.clear()

        val items = buildList {
            CsvParser.parse(
                text = csvText,
            ) { row ->
                val tagIds: List<String> = resolveTagIds(
                    raw = row.get("Tags"),
                    vaultId = vaultId,
                    separator = ';',
                )

                add(
                    Item.create(
                        vaultId = vaultId,
                        tagIds = tagIds,
                        contentType = ItemContentType.Login,
                        content = ItemContent.Login.create(
                            name = row.get("Title"),
                            username = row.get("Username"),
                            password = row.get("Password"),
                            url = row.get("Url"),
                            notes = row.get("Notes"),
                        ),
                    ),
                )
            }
        }

        return ImportContent(
            items = items,
            tags = tags,
            unknownItems = 0,
        )
    }

    private fun readContentFrom1Pux(jsonText: String?, vaultId: String): ImportContent {
        var unknownItems = 0
        tags.clear()

        if (jsonText.isNullOrBlank()) {
            return ImportContent(
                items = emptyList(),
                tags = emptyList(),
                unknownItems = unknownItems,
            )
        }

        val model = json.decodeFromString<OnePassword1Pux>(jsonText)

        // First pass: collect all unique tags and create them
        val tagNameToId = mutableMapOf<String, String>()

        model.accounts?.forEach { account ->
            account.vaults?.forEach { vault ->
                vault.items?.forEach { item ->
                    // Skip trashed items
                    if (item.trashed == true || item.state == "archived") return@forEach

                    item.overview?.tags?.forEach { tagName ->
                        val trimmedTag = tagName.trim().takeIf { it.isNotBlank() }
                        if (trimmedTag != null && !tagNameToId.containsKey(trimmedTag)) {
                            // Use resolveTagIds with single tag to create it
                            val tagIds = resolveTagIds(trimmedTag, vaultId, ';')
                            if (tagIds.isNotEmpty()) {
                                tagNameToId[trimmedTag] = tagIds.first()
                            }
                        }
                    }
                }
            }
        }

        // Second pass: import items with tag references
        val items = buildList {
            model.accounts?.forEach { account ->
                account.vaults?.forEach { vault ->
                    vault.items?.forEach { item ->
                        // Skip trashed items
                        if (item.trashed == true || item.state == "archived") return@forEach

                        // Resolve tag IDs for this item
                        val itemTagIds = item.overview?.tags?.mapNotNull { tagName ->
                            tagName.trim().takeIf { it.isNotBlank() }?.let { tagNameToId[it] }
                        }?.takeIf { it.isNotEmpty() }

                        val categoryUuid = item.categoryUuid ?: ""

                        when (categoryUuid) {
                            CATEGORY_LOGIN, CATEGORY_PASSWORD -> {
                                parseLogin(item, vaultId, itemTagIds)?.let { add(it) }
                            }

                            CATEGORY_SECURE_NOTE -> {
                                parseSecureNote(item, vaultId, itemTagIds)?.let { add(it) }
                            }

                            CATEGORY_CREDIT_CARD -> {
                                parseCreditCard(item, vaultId, itemTagIds)?.let { add(it) }
                            }

                            CATEGORY_IDENTITY -> {
                                // Convert identity to secure note
                                unknownItems++
                                parseAsSecureNote(item, vaultId, "Identity", itemTagIds)?.let { add(it) }
                            }

                            else -> {
                                // For unknown categories, try to import as login if it has login fields
                                // Otherwise convert to secure note
                                if (item.details?.loginFields?.isNotEmpty() == true) {
                                    parseLogin(item, vaultId, itemTagIds)?.let { add(it) }
                                } else {
                                    unknownItems++
                                    parseAsSecureNote(item, vaultId, "Item", itemTagIds)?.let { add(it) }
                                }
                            }
                        }
                    }
                }
            }
        }

        return ImportContent(
            items = items,
            tags = tags,
            unknownItems = unknownItems,
        )
    }

    private fun parseLogin(item: OnePasswordItem, vaultId: String, tagIds: List<String>?): com.twofasapp.core.common.domain.items.Item? {
        val name = item.overview?.title?.trim()?.takeIf { it.isNotBlank() }
        val notes = item.details?.notesPlain?.trim()?.takeIf { it.isNotBlank() }

        // Extract username and password from loginFields
        var username: String? = null
        var password: String? = null

        item.details?.loginFields?.forEach { field ->
            when (field.designation) {
                "username" -> username = field.value?.trim()?.takeIf { it.isNotBlank() }
                "password" -> password = field.value?.trim()?.takeIf { it.isNotBlank() }
            }
        }

        // Extract URLs
        val uris: List<ItemUri>? = when {
            item.overview?.urls?.isNotEmpty() == true -> {
                item.overview.urls.mapNotNull { urlEntry ->
                    urlEntry.url?.trim()?.takeIf { it.isNotBlank() }?.let {
                        ItemUri(text = it, matcher = UriMatcher.Domain)
                    }
                }.takeIf { it.isNotEmpty() }
            }

            item.overview?.url?.trim()?.takeIf { it.isNotBlank() } != null -> {
                listOf(ItemUri(text = item.overview.url.trim(), matcher = UriMatcher.Domain))
            }

            else -> null
        }

        // Extract additional fields from sections
        val additionalFields = mutableListOf<String>()
        item.details?.sections?.forEach { section ->
            section.fields?.forEach { field ->
                val title = field.title
                val value = field.value?.stringValue?.trim()?.takeIf { it.isNotBlank() }
                if (title != null && value != null) {
                    additionalFields.add("$title: $value")
                }
            }
        }

        val additionalInfo = additionalFields.takeIf { it.isNotEmpty() }?.joinToString("\n")
        val mergedNotes = mergeNotes(notes, additionalInfo)

        return Item.create(
            vaultId = vaultId,
            tagIds = tagIds.orEmpty(),
            contentType = ItemContentType.Login,
            content = ItemContent.Login.Empty.copy(
                name = name.orEmpty(),
                username = username,
                password = password?.let { SecretField.ClearText(it) },
                notes = mergedNotes,
                iconType = IconType.Icon,
                iconUriIndex = if (uris == null) null else 0,
                uris = uris.orEmpty(),
            ),
        )
    }

    private fun parseSecureNote(item: OnePasswordItem, vaultId: String, tagIds: List<String>?): com.twofasapp.core.common.domain.items.Item? {
        val name = item.overview?.title?.trim()?.takeIf { it.isNotBlank() }
        val noteText = item.details?.notesPlain?.trim()?.takeIf { it.isNotBlank() }

        // Extract additional fields from sections
        val additionalFields = mutableListOf<String>()
        item.details?.sections?.forEach { section ->
            section.fields?.forEach { field ->
                val title = field.title
                val value = field.value?.stringValue?.trim()?.takeIf { it.isNotBlank() }
                if (title != null && value != null) {
                    additionalFields.add("$title: $value")
                }
            }
        }

        val additionalInfo = additionalFields.takeIf { it.isNotEmpty() }?.joinToString("\n")
        val fullText = mergeNotes(noteText, additionalInfo)

        return Item.create(
            vaultId = vaultId,
            tagIds = tagIds.orEmpty(),
            contentType = ItemContentType.SecureNote,
            content = ItemContent.SecureNote(
                name = name.orEmpty(),
                text = fullText?.let { SecretField.ClearText(it) },
                additionalInfo = null,
            ),
        )
    }

    private fun parseCreditCard(item: OnePasswordItem, vaultId: String, tagIds: List<String>?): com.twofasapp.core.common.domain.items.Item? {
        val name = item.overview?.title?.trim()?.takeIf { it.isNotBlank() }
        val notes = item.details?.notesPlain?.trim()?.takeIf { it.isNotBlank() }

        // Extract card details from sections
        var cardNumberString: String? = null
        var cardHolder: String? = null
        var expirationDateString: String? = null
        var securityCodeString: String? = null
        var pinCode: String? = null
        val additionalFields = mutableListOf<String>()

        item.details?.sections?.forEach { section ->
            section.fields?.forEach { field ->
                val fieldId = field.id?.lowercase() ?: ""
                val fieldTitle = field.title?.lowercase() ?: ""

                when {
                    // Card number
                    fieldId == "ccnum" || fieldTitle.contains("card number") -> {
                        cardNumberString = field.value?.creditCardNumber ?: field.value?.stringValue
                    }
                    // Cardholder name
                    fieldId == "cardholder" || fieldTitle.contains("cardholder") -> {
                        cardHolder = field.value?.stringValue
                    }
                    // Expiration date (stored as YYYYMM integer)
                    fieldId == "expiry" || fieldTitle.contains("expir") -> {
                        expirationDateString = field.value?.monthYear?.let { monthYear ->
                            // Convert YYYYMM to MM/YY format
                            val year = monthYear / 100
                            val month = monthYear % 100
                            val shortYear = year % 100
                            String.format("%02d/%02d", month, shortYear)
                        } ?: field.value?.stringValue
                    }
                    // Security code (CVV)
                    fieldId == "cvv" || fieldTitle.contains("security code") || fieldTitle.contains("cvv") -> {
                        securityCodeString = field.value?.concealed ?: field.value?.stringValue
                    }
                    // PIN code
                    fieldId == "pin" || fieldTitle.contains("pin") -> {
                        pinCode = field.value?.concealed ?: field.value?.stringValue
                    }
                    // Other fields
                    else -> {
                        val value = field.value?.stringValue?.trim()?.takeIf { it.isNotBlank() }
                        val title = field.title?.trim()?.takeIf { it.isNotBlank() }
                        if (value != null && title != null) {
                            additionalFields.add("$title: $value")
                        }
                    }
                }
            }
        }

        val cardNumber = cardNumberString?.trim()?.removeWhitespace()?.takeIf { it.isNotBlank() }?.let { SecretField.ClearText(it) }
        val expirationDate = expirationDateString?.trim()?.takeIf { it.isNotBlank() }?.let { SecretField.ClearText(it) }
        val securityCode = securityCodeString?.trim()?.takeIf { it.isNotBlank() }?.let { SecretField.ClearText(it) }
        val cardNumberMask = cardNumberString?.removeWhitespace()?.let { detectCardNumberMask(it) }
        val cardIssuer = cardNumberString?.let { detectCardIssuer(it) }

        // Add PIN code to additional fields if present
        pinCode?.trim()?.takeIf { it.isNotBlank() }?.let {
            additionalFields.add(0, "PIN: $it")
        }

        val additionalInfo = additionalFields.takeIf { it.isNotEmpty() }?.joinToString("\n")
        val mergedNotes = mergeNotes(notes, additionalInfo)

        return Item.create(
            vaultId = vaultId,
            tagIds = tagIds.orEmpty(),
            contentType = ItemContentType.PaymentCard,
            content = ItemContent.PaymentCard.Empty.copy(
                name = name.orEmpty(),
                cardHolder = cardHolder?.trim()?.takeIf { it.isNotBlank() },
                cardIssuer = cardIssuer,
                cardNumber = cardNumber,
                cardNumberMask = cardNumberMask,
                expirationDate = expirationDate,
                securityCode = securityCode,
                notes = mergedNotes,
            ),
        )
    }

    private fun parseAsSecureNote(
        item: OnePasswordItem,
        vaultId: String,
        typeName: String,
        tagIds: List<String>?,
    ): Item? {
        val itemName = item.overview?.title?.trim()?.takeIf { it.isNotBlank() }

        val displayName = if (itemName != null) {
            "$itemName ($typeName)"
        } else {
            "($typeName)"
        }

        // Extract all fields from sections as additional info
        val allFields = mutableListOf<String>()
        item.details?.sections?.forEach { section ->
            section.fields?.forEach { field ->
                val value = field.value?.stringValue?.trim()?.takeIf { it.isNotBlank() }
                if (value != null) {
                    val title = field.title?.trim()?.takeIf { it.isNotBlank() }
                    if (title != null) {
                        allFields.add("${title.replaceFirstChar { it.uppercase() }}: $value")
                    } else {
                        allFields.add(value)
                    }
                }
            }
        }

        val fieldsInfo = allFields.takeIf { it.isNotEmpty() }?.joinToString("\n")
        val noteText = item.details?.notesPlain?.trim()?.takeIf { it.isNotBlank() }
        val fullText = mergeNotes(fieldsInfo, noteText)

        return Item.create(
            vaultId = vaultId,
            tagIds = tagIds.orEmpty(),
            contentType = ItemContentType.SecureNote,
            content = ItemContent.SecureNote(
                name = displayName,
                text = fullText?.let { SecretField.ClearText(it) },
                additionalInfo = null,
            ),
        )
    }

    private fun mergeNotes(note1: String?, note2: String?): String? {
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

    companion object {
        private const val CATEGORY_LOGIN = "001"
        private const val CATEGORY_CREDIT_CARD = "002"
        private const val CATEGORY_SECURE_NOTE = "003"
        private const val CATEGORY_IDENTITY = "004"
        private const val CATEGORY_PASSWORD = "005"
    }

    // 1Password 1PUX Format Models
    @Serializable
    private data class OnePassword1Pux(
        val accounts: List<Account>? = null,
    )

    @Serializable
    private data class Account(
        val attrs: AccountAttrs? = null,
        val vaults: List<Vault>? = null,
    )

    @Serializable
    private data class AccountAttrs(
        val accountName: String? = null,
        val name: String? = null,
        val email: String? = null,
        val uuid: String? = null,
        val domain: String? = null,
    )

    @Serializable
    private data class Vault(
        val attrs: VaultAttrs? = null,
        val items: List<OnePasswordItem>? = null,
    )

    @Serializable
    private data class VaultAttrs(
        val uuid: String? = null,
        val name: String? = null,
        val desc: String? = null,
        val type: String? = null,
    )

    @Serializable
    private data class OnePasswordItem(
        val uuid: String? = null,
        val favIndex: Int? = null,
        val createdAt: Int? = null,
        val updatedAt: Int? = null,
        val trashed: Boolean? = null,
        val state: String? = null,
        val categoryUuid: String? = null,
        val details: ItemDetails? = null,
        val overview: ItemOverview? = null,
    )

    @Serializable
    private data class ItemDetails(
        val loginFields: List<LoginField>? = null,
        val notesPlain: String? = null,
        val sections: List<Section>? = null,
        val passwordHistory: List<PasswordHistoryEntry>? = null,
    )

    @Serializable
    private data class LoginField(
        val value: String? = null,
        val name: String? = null,
        val fieldType: String? = null,
        val designation: String? = null,
    )

    @Serializable
    private data class Section(
        val title: String? = null,
        val name: String? = null,
        val fields: List<SectionField>? = null,
    )

    @Serializable
    private data class SectionField(
        val title: String? = null,
        val id: String? = null,
        val value: SectionFieldValue? = null,
    )

    @Serializable
    private data class SectionFieldValue(
        val concealed: String? = null,
        val string: String? = null,
        val totp: String? = null,
        val date: Int? = null,
        val monthYear: Int? = null,
        val creditCardType: String? = null,
        val creditCardNumber: String? = null,
        val phone: String? = null,
        val url: String? = null,
    ) {
        val stringValue: String?
            get() = string ?: concealed ?: totp ?: phone ?: url ?: creditCardNumber ?: creditCardType
    }

    @Serializable
    private data class PasswordHistoryEntry(
        val value: String? = null,
        val time: Int? = null,
    )

    @Serializable
    private data class ItemOverview(
        val title: String? = null,
        val subtitle: String? = null,
        val url: String? = null,
        val urls: List<ItemURL>? = null,
        val tags: List<String>? = null,
    )

    @Serializable
    private data class ItemURL(
        val label: String? = null,
        val url: String? = null,
    )
}