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
import com.twofasapp.core.common.domain.ImportType
import com.twofasapp.core.common.domain.SecretField
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
import com.twofasapp.feature.externalimport.import.TransferUtils

internal class LastPassImportSpec(
    private val vaultsRepository: VaultsRepository,
    private val context: Context,
) : ImportSpec() {
    override val type = ImportType.LastPass
    override val name = "LastPass"
    override val image = com.twofasapp.core.design.R.drawable.external_logo_lastpass
    override val instructions = context.getString(R.string.transfer_instructions_lastpass)
    override val additionalInfo = null
    override val cta: List<Cta> = listOf(
        Cta.Primary(
            text = context.getString(R.string.transfer_instructions_cta_csv),
            action = CtaAction.ChooseFile,
        ),
    )

    override suspend fun readContent(uri: Uri): ImportContent {
        val vaultId = vaultsRepository.getVault().id
        var unknownItems = 0
        tags.clear()

        val items = buildList {
            CsvParser.parse(
                text = context.readTextFile(uri),
            ) { row ->

                val tagIds: List<String> = resolveTagIds(
                    raw = row.get("grouping"),
                    vaultId = vaultId,
                    separator = '\\',
                )

                if (row.get("url").orEmpty().startsWith("http://sn", ignoreCase = true)) {
                    val extras = row.get("extra").orEmpty()
                    val fields = parseExtraFields(extras)
                    val noteType = fields["NoteType"]

                    when (noteType) {
                        "Credit Card" -> {
                            val name = row.get("name")?.trim()?.takeIf { it.isNotBlank() }
                            val cardHolder = fields["Name on Card"]?.trim()?.takeIf { it.isNotBlank() }
                            val cardNumberString = fields["Number"]?.trim()?.takeIf { it.isNotBlank() }?.removeWhitespace()
                            val securityCodeString = fields["Security Code"]?.trim()?.takeIf { it.isNotBlank() }?.removeWhitespace()
                            val expirationDateString = parseExpirationDate(fields["Expiration Date"])
                            val noteText = fields["Notes"]?.trim()?.takeIf { it.isNotBlank() }

                            val cardNumber = cardNumberString?.let { SecretField.ClearText(it) }
                            val expirationDate = expirationDateString?.let { SecretField.ClearText(it) }
                            val securityCode = securityCodeString?.let { SecretField.ClearText(it) }
                            val cardNumberMask = cardNumberString?.let { detectCardNumberMask(it) }
                            val cardIssuer = cardNumberString?.let { detectCardIssuer(it) }

                            // Build additional info from unknown extra fields
                            val excludedExtraKeys = setOf(
                                "NoteType",
                                "Language",
                                "Name on Card",
                                "Type",
                                "Number",
                                "Security Code",
                                "Expiration Date",
                                "Notes",
                            )
                            val extraAdditionalInfo = fields.filterKeys { !excludedExtraKeys.contains(it) }

                            // Build additional info from unknown CSV columns
                            val knownCSVColumns = setOf("url", "username", "password", "extra", "name", "grouping", "fav")
                            val csvAdditionalInfo = row.map.filterKeys { !knownCSVColumns.contains(it) }

                            // Merge all additional info
                            val combinedAdditionalInfo = TransferUtils.formatNote(
                                note = null,
                                fields = extraAdditionalInfo + csvAdditionalInfo,
                            )
                            val mergedNotes = TransferUtils.formatNote(
                                note = noteText,
                                fields = mapOfNotNull(combinedAdditionalInfo?.let { "Additional Info" to it }),
                            )

                            add(
                                Item.create(
                                    contentType = ItemContentType.PaymentCard,
                                    vaultId = vaultId,
                                    tagIds = tagIds,
                                    content = ItemContent.PaymentCard.Empty.copy(
                                        name = name.orEmpty(),
                                        cardHolder = cardHolder,
                                        cardIssuer = cardIssuer,
                                        cardNumber = cardNumber,
                                        cardNumberMask = cardNumberMask,
                                        expirationDate = expirationDate,
                                        securityCode = securityCode,
                                        notes = mergedNotes,
                                    ),
                                ),
                            )
                        }

                        null -> {
                            // Secure note without NoteType
                            val knownCSVColumns = setOf("url", "username", "password", "extra", "name", "grouping", "fav")
                            val csvAdditionalInfo = row.map.filterKeys { !knownCSVColumns.contains(it) }

                            val fullText = TransferUtils.formatNote(
                                note = extras.normalizeExtraLine(),
                                fields = csvAdditionalInfo,
                            )

                            add(
                                Item.create(
                                    vaultId = vaultId,
                                    tagIds = tagIds,
                                    contentType = ItemContentType.SecureNote,
                                    content = ItemContent.SecureNote.create(
                                        name = row.get("name"),
                                        text = fullText,
                                    ),
                                ),
                            )
                        }

                        else -> {
                            // Unknown types -> secure note
                            val itemName = row.get("name")?.trim()?.takeIf { it.isNotBlank() }
                            val displayName = if (itemName != null) {
                                "$itemName ($noteType)"
                            } else {
                                "($noteType)"
                            }

                            // Build additional info from unknown extra fields
                            val excludedExtraKeys = setOf("NoteType", "Notes", "Language")
                            val extraAdditionalInfo = fields.filterKeys { !excludedExtraKeys.contains(it) }

                            // Build additional info from unknown CSV columns
                            val knownCSVColumns = setOf("url", "username", "password", "extra", "name", "grouping", "fav")
                            val csvAdditionalInfo = row.map.filterKeys { !knownCSVColumns.contains(it) }

                            // Merge all additional info
                            val noteText = fields["Notes"]?.trim()?.takeIf { it.isNotBlank() }
                            val combinedAdditionalInfo = TransferUtils.formatNote(
                                note = null,
                                fields = extraAdditionalInfo + csvAdditionalInfo,
                            )
                            val fullText = TransferUtils.formatNote(
                                note = combinedAdditionalInfo,
                                fields = mapOfNotNull(noteText?.let { "Notes" to it }),
                            )

                            add(
                                Item.create(
                                    vaultId = vaultId,
                                    tagIds = tagIds,
                                    contentType = ItemContentType.SecureNote,
                                    content = ItemContent.SecureNote.create(
                                        name = displayName,
                                        text = fullText,
                                    ),
                                ),
                            )

                            unknownItems++
                        }
                    }
                } else {
                    // Add login
                    val knownCSVColumns = setOf("url", "username", "password", "extra", "name", "grouping", "fav")
                    val csvAdditionalInfo = row.map.filterKeys { !knownCSVColumns.contains(it) }

                    val noteWithAdditionalInfo = TransferUtils.formatNote(
                        note = row.get("extra")?.normalizeExtraLine(),
                        fields = csvAdditionalInfo,
                    )

                    add(
                        Item.create(
                            vaultId = vaultId,
                            tagIds = tagIds,
                            contentType = ItemContentType.Login,
                            content = ItemContent.Login.create(
                                name = row.get("name"),
                                username = row.get("username"),
                                password = row.get("password"),
                                url = row.get("url"),
                                notes = noteWithAdditionalInfo,
                            ),
                        ),
                    )
                }
            }
        }

        return ImportContent(
            items = items,
            tags = tags,
            unknownItems = unknownItems,
        )
    }

    private fun String.normalizeExtraLine(): String =
        this.lines()
            .joinToString("\n") { it.replaceFirst(":", ": ") }

    private fun parseExtraFields(extra: String): Map<String, String> {
        val fields = mutableMapOf<String, String>()
        val lines = extra.lines()

        for (line in lines) {
            val colonIndex = line.indexOf(':')
            if (colonIndex > 0) {
                val key = line.substring(0, colonIndex)
                val value = line.substring(colonIndex + 1)
                fields[key] = value
            }
        }

        return fields
    }

    private fun parseExpirationDate(dateString: String?): String? {
        // Format: "November,2030" -> "11/30"
        if (dateString == null) return null

        val parts = dateString.split(",")
        if (parts.size != 2) return null

        val monthNumber = monthNumber(parts[0].trim()) ?: return null
        val yearString = parts[1].trim()
        val year = yearString.takeLast(2).padStart(2, '0')

        return String.format("%02d/%s", monthNumber, year)
    }

    private fun monthNumber(monthName: String): Int? {
        return when (monthName) {
            "January" -> 1
            "February" -> 2
            "March" -> 3
            "April" -> 4
            "May" -> 5
            "June" -> 6
            "July" -> 7
            "August" -> 8
            "September" -> 9
            "October" -> 10
            "November" -> 11
            "December" -> 12
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

    private fun <K, V> mapOfNotNull(vararg pairs: Pair<K, V>?): Map<K, V> {
        return pairs.filterNotNull().toMap()
    }
}