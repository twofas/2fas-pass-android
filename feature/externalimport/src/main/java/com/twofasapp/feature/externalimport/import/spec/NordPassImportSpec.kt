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
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.common.ktx.formatDate
import com.twofasapp.core.common.ktx.readTextFile
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.feature.externalimport.import.CsvParser
import com.twofasapp.feature.externalimport.import.CsvRow
import com.twofasapp.feature.externalimport.import.ImportContent
import com.twofasapp.feature.externalimport.import.ImportSpec
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant

internal class NordPassImportSpec(
    private val context: Context,
    private val vaultsRepository: VaultsRepository,
    private val json: Json,
) : ImportSpec() {

    override val type = ImportType.NordPass
    override val name = ImportType.NordPass.displayName
    override val image = com.twofasapp.core.design.R.drawable.external_logo_nordpass
    override val instructions =
        context.getString(com.twofasapp.core.locale.R.string.transfer_instructions_nordpass)
    override val additionalInfo = null
    override val cta = listOf(
        Cta.Primary(
            text = context.getString(com.twofasapp.core.locale.R.string.transfer_instructions_cta_csv),
            action = CtaAction.ChooseFile,
        ),
    )

    override suspend fun readContent(uri: Uri): ImportContent {
        val vaultId = vaultsRepository.getVault().id

        val parsedItems = mutableListOf<ParsedItem>()
        val tags = mutableListOf<Tag>()
        var unknownItems = 0

        CsvParser.parse(
            text = context.readTextFile(uri),
        ) { row ->
            when (row.getType()) {
                RowType.CreditCard -> parsedItems.add(parsePaymentCard(row, vaultId))
                RowType.Folder -> tags.add(parseTag(row, vaultId))
                RowType.Password -> parsedItems.add(parseLogin(row, vaultId))
                RowType.Note -> parsedItems.add(parseSecureNote(row, vaultId))
                RowType.Document,
                RowType.Identity,
                is RowType.Unknown -> {
                    unknownItems++
                    parsedItems.add(parseSecureNote(row, vaultId))
                }
            }
        }

        return ImportContent(
            items = parsedItems.map { it.resolve(tags) },
            tags = tags,
            unknownItems = unknownItems,
        )
    }

    private fun parsePaymentCard(row: CsvRow, vaultId: String): ParsedItem {
        return ParsedItem(
            item = Item.create(
                vaultId = vaultId,
                contentType = ItemContentType.PaymentCard,
                content = ItemContent.PaymentCard(
                    name = row.getColumn(Column.Name) ?: "",
                    cardHolder = row.getColumn(Column.CardholderName),
                    cardNumber = row.getColumn(Column.CardNumber)
                        ?.let { SecretField.ClearText(it) },
                    cardNumberMask = row.getColumn(Column.CardNumber)
                        ?.let { detectCardNumberMask(it) },
                    expirationDate = row.getColumn(Column.ExpiryDate)
                        ?.let {
                            detectCardExpiryDate(it)?.let { expiryDate ->
                                SecretField.ClearText(
                                    expiryDate
                                )
                            }
                        },
                    securityCode = row.getColumn(Column.Cvc)?.let { SecretField.ClearText(it) },
                    cardIssuer = row.getColumn(Column.CardNumber)?.let { detectCardIssuer(it) },
                    notes = createNote(
                        row = row,
                        excludeColumns = setOf(
                            Column.Name,
                            Column.CardholderName,
                            Column.CardNumber,
                            Column.ExpiryDate,
                            Column.Cvc
                        )
                    ),
                )
            ),
            tagName = row.getTagName()
        )
    }

    private fun detectCardExpiryDate(expiryDateRow: String): String? {
        val expirationCardRegex = Regex("""(\d+)/(\d+)/(\d+)""")

        val match = expirationCardRegex.find(expiryDateRow)
        val month = match?.groupValues?.getOrNull(1)?.trim()
        val year = match?.groupValues?.getOrNull(2)?.trim()

        if (month == null || year == null) {
            return null
        }

        val validMonth = when {
            month.length == 2 -> month
            month.length == 1 -> "0$month"
            else -> null
        }

        if (validMonth == null) {
            return null
        }

        val validYear = when {
            year.length == 2 -> year
            year.length == 4 -> year.subSequence(2, 4)
            else -> null
        }
        return "$validMonth/$validYear"
    }

    private fun parseLogin(row: CsvRow, vaultId: String): ParsedItem {
        return ParsedItem(
            item = Item.create(
                vaultId = vaultId,
                contentType = ItemContentType.Login,
                content = ItemContent.Login(
                    name = row.getColumn(Column.Name) ?: "",
                    username = row.getColumn(Column.Username),
                    password = row.getColumn(Column.Password)
                        ?.let { SecretField.ClearText(it) },
                    uris = detectUris(
                        row.getColumn(Column.Url),
                        row.getColumn(Column.AdditionalUrls)
                    ),
                    iconType = IconType.Icon,
                    iconUriIndex = null,
                    customImageUrl = null,
                    labelText = null,
                    labelColor = null,
                    notes = createNote(
                        row = row,
                        excludeColumns = setOf(
                            Column.Name,
                            Column.Username,
                            Column.Password,
                            Column.Url,
                            Column.AdditionalUrls
                        )
                    ),
                )
            ),
            tagName = row.getTagName()
        )
    }

    private fun detectUris(url: String?, additionalUrls: String?): List<ItemUri> {
        return buildList {
            add(url)
            additionalUrls?.let { urls ->
                try {
                    addAll(json.decodeFromString<List<String>>(urls))
                } catch (t: Throwable) {
                    //ignore urls
                }
            }
        }.filterNotNull()
            .filter { url -> url.isNotBlank() }
            .map { url -> ItemUri(text = url.trim()) }
    }

    private fun parseSecureNote(row: CsvRow, vaultId: String): ParsedItem {
        return ParsedItem(
            item = Item.create(
                vaultId = vaultId,
                contentType = ItemContentType.SecureNote,
                content = ItemContent.SecureNote(
                    name = row.getColumn(Column.Name) ?: "",
                    text = row.getColumn(Column.Note)
                        ?.let { SecretField.ClearText(it) },
                    additionalInfo = createNote(
                        row = row,
                        excludeColumns = setOf(
                            Column.Name,
                            Column.Note,
                        )
                    ),
                )
            ),
            tagName = row.getTagName()
        )
    }

    private fun parseTag(row: CsvRow, vaultId: String): Tag {
        return Tag.create(
            vaultId = vaultId,
            id = Uuid.generate(),
            name = row.getColumn(Column.Name)
        )
    }

    private fun createNote(row: CsvRow, excludeColumns: Set<Column>): String? {
        val allExcludedColumns = buildSet {
            addAll(excludeColumns)
            add(Column.Type)
            add(Column.Folder)
        }
        return buildString {
            row.map
                .mapKeys { (key, _) -> Column.fromValue(key) }
                .filterKeys { key -> allExcludedColumns.contains(key).not() }
                .mapValues { (_, value) -> value.trim() }
                .filterValues { value -> value.isNotBlank() }
                .map { (key, value) ->
                    when (key) {
                        Column.CustomFields -> parseCustomField(value).forEach { customFieldValue ->
                            appendLine(customFieldValue)
                        }

                        Column.AdditionalUrls,
                        Column.Address1,
                        Column.Address2,
                        Column.CardNumber,
                        Column.CardholderName,
                        Column.City,
                        Column.Country,
                        Column.Cvc,
                        Column.Email,
                        Column.ExpiryDate,
                        Column.Folder,
                        Column.FullName,
                        Column.Name,
                        Column.Note,
                        Column.Password,
                        Column.PhoneNumber,
                        Column.Pin,
                        Column.State,
                        Column.Type,
                        is Column.Unknown,
                        Column.Url,
                        Column.Username,
                        Column.Zipcode -> appendLine("${key.value}: $value")
                    }
                }
        }
    }

    private fun parseCustomField(value: String): List<String> {
        val customFields = json.decodeFromString<List<CustomField>>(value)
        return customFields.mapNotNull { customField ->
            val label = customField.label?.trim()
            if (label.isNullOrBlank()) {
                return@mapNotNull null
            }
            val value = customField.value?.trim()
            if (value.isNullOrBlank()) {
                return@mapNotNull null
            }
            val type = customField.type?.trim()?.let { CustomFieldType.fromValue(it) }
            if (type == null) {
                return@mapNotNull null
            }

            when (type) {
                CustomFieldType.Date -> {
                    val asLong = value.toLongOrNull()
                    if (asLong == null) {
                        "${label}: $value"
                    } else {
                        val date = Instant.ofEpochSecond(asLong).formatDate()
                        "${label}: $date"
                    }
                }

                CustomFieldType.Hidden,
                CustomFieldType.Text,
                is CustomFieldType.Unknown -> "${label}: $value"
            }
        }
    }

    private fun CsvRow.getColumn(column: Column): String? {
        return get(column.value)
    }

    private fun CsvRow.getType(): RowType {
        return RowType.fromValue(getColumn(Column.Type) ?: "")
    }

    private fun CsvRow.getTagName(): String? {
        return getColumn(Column.Folder)
    }

    private data class ParsedItem(
        val item: Item,
        val tagName: String?
    ) {
        fun resolve(tags: List<Tag>): Item {
            return item.copy(
                tagIds = tags.filter { tag -> tagName == tag.name }.map { it.id }
            )
        }
    }

    @Serializable
    private data class CustomField(
        @SerialName("type")
        val type: String? = null,
        @SerialName("label")
        val label: String? = null,
        @SerialName("value")
        val value: String? = null,
    )

    private sealed interface CustomFieldType {
        val value: String

        data object Text : CustomFieldType {
            override val value = "text"
        }

        data object Date : CustomFieldType {
            override val value = "date"
        }

        data object Hidden : CustomFieldType {
            override val value = "hidden"
        }

        data class Unknown(override val value: String) : CustomFieldType

        companion object Companion {
            fun values(): List<CustomFieldType> {
                return listOf(
                    Text,
                    Date,
                    Hidden
                )
            }

            fun fromValue(value: String): CustomFieldType {
                return values().firstOrNull { it.value == value } ?: Unknown(value)
            }
        }
    }

    private sealed interface RowType {
        val value: String

        data object Password : RowType {
            override val value = "password"
        }

        data object Folder : RowType {
            override val value = "folder"
        }

        data object Note : RowType {
            override val value = "note"
        }

        data object CreditCard : RowType {
            override val value = "credit_card"
        }

        data object Identity : RowType {
            override val value = "identity"
        }

        data object Document : RowType {
            override val value = "document"
        }

        data class Unknown(override val value: String) : RowType

        companion object Companion {
            fun values(): List<RowType> {
                return listOf(
                    Password,
                    Folder,
                    Note,
                    CreditCard,
                    Identity,
                    Document
                )
            }

            fun fromValue(value: String): RowType {
                return values().firstOrNull { it.value == value } ?: Unknown(value)
            }
        }
    }

    private sealed interface Column {

        val value: String

        data object Name : Column {
            override val value = "name"
        }

        data object Url : Column {
            override val value = "url"
        }

        data object AdditionalUrls : Column {
            override val value = "additional_urls"
        }

        data object Username : Column {
            override val value = "username"
        }

        data object Password : Column {
            override val value = "password"
        }

        data object Note : Column {
            override val value = "note"
        }

        data object CardholderName : Column {
            override val value = "cardholdername"
        }

        data object CardNumber : Column {
            override val value = "cardnumber"
        }

        data object Cvc : Column {
            override val value = "cvc"
        }

        data object Pin : Column {
            override val value = "pin"
        }

        data object ExpiryDate : Column {
            override val value = "expirydate"
        }

        data object Zipcode : Column {
            override val value = "zipcode"
        }

        data object Folder : Column {
            override val value = "folder"
        }

        data object FullName : Column {
            override val value = "full_name"
        }

        data object PhoneNumber : Column {
            override val value = "phone_number"
        }

        data object Email : Column {
            override val value = "email"
        }

        data object Address1 : Column {
            override val value = "address1"
        }

        data object Address2 : Column {
            override val value = "address2"
        }

        data object City : Column {
            override val value = "city"
        }

        data object Country : Column {
            override val value = "country"
        }

        data object State : Column {
            override val value = "state"
        }

        data object Type : Column {
            override val value = "type"
        }

        data object CustomFields : Column {
            override val value = "custom_fields"
        }

        data class Unknown(override val value: String) : Column

        companion object Companion {
            fun values(): List<Column> {
                return listOf(
                    Name,
                    Url,
                    AdditionalUrls,
                    Username,
                    Password,
                    Note,
                    CardholderName,
                    CardNumber,
                    Cvc,
                    Pin,
                    ExpiryDate,
                    Zipcode,
                    Folder,
                    FullName,
                    PhoneNumber,
                    Email,
                    Address1,
                    Address2,
                    City,
                    Country,
                    State,
                    Type,
                    CustomFields
                )
            }

            fun fromValue(value: String): Column {
                return values().firstOrNull { it.value == value } ?: Unknown(value)
            }
        }
    }
}