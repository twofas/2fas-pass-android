/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.externalimport.import

import com.opencsv.CSVParser
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import com.twofasapp.core.common.domain.IconType
import com.twofasapp.core.common.domain.ItemUri
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.UriMatcher
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import java.io.StringReader

internal data class CsvFile(
    val text: String,
    val delimiter: Char,
    val schemas: List<Schema>,
) {
    private val csvRecords: List<List<String>> by lazy {
        val csvReader = CSVReaderBuilder(StringReader(text))
            .withCSVParser(
                CSVParserBuilder()
                    .withSeparator(delimiter)
                    .withQuoteChar('"')
                    .withEscapeChar(CSVParser.NULL_CHARACTER)
                    .build(),
            )
            .build()

        csvReader.use {
            val rows = csvReader.readAll()
            val expectedColumnCount = rows.firstOrNull()?.size ?: 0

            rows.map { row ->
                row.toMutableList().apply {
                    while (size < expectedColumnCount) {
                        add("") // fill missing columns with empty strings
                    }
                }
            }
        }
    }

    sealed interface Schema {
        val contentType: ItemContentType

        fun getColumnNames(): List<List<String>>

        data class Login(
            val name: List<String>,
            val url: List<String>,
            val username: List<String>,
            val password: List<String>,
            val notes: List<String>,
        ) : Schema {
            override val contentType = ItemContentType.Login

            override fun getColumnNames(): List<List<String>> {
                return listOf(name, url, username, password, notes)
            }
        }

        data class SecureNote(
            val name: List<String>,
            val text: List<String>,
        ) : Schema {
            override val contentType = ItemContentType.SecureNote

            override fun getColumnNames(): List<List<String>> {
                return listOf(name, text)
            }
        }

        data class PaymentCard(
            val name: List<String>,
            val cardholder: List<String>,
            val number: List<String>,
            val expiration: List<String>,
            val cvv: List<String>,
            val notes: List<String>,
        ) : Schema {
            override val contentType = ItemContentType.PaymentCard

            override fun getColumnNames(): List<List<String>> {
                return listOf(name, cardholder, number, expiration, cvv, notes)
            }
        }
    }

    private fun indexOf(columnNames: List<String>): List<Int> {
        val lowercaseColumns = csvRecords.first().map { it.lowercase() }
        val indices = columnNames.mapNotNull { columnName ->
            val index = lowercaseColumns.indexOf(columnName.lowercase())
            if (index != -1) index else null
        }

        return indices
    }

    private data class SchemaMatch(
        val schema: Schema,
        val matchedColumns: Int,
        val totalColumns: Int,
    ) {
        val matchScore: Float
            get() = if (totalColumns > 0) matchedColumns.toFloat() / totalColumns.toFloat() else 0f
    }

    private fun findBestMatchingSchema(): Schema {
        if (csvRecords.isEmpty()) {
            return schemas.first()
        }

        val lowercaseColumns = csvRecords.first().map { it.lowercase() }

        val matches = schemas.map { schema ->
            val columnNames = schema.getColumnNames()
            val matchedColumns = columnNames.count { possibleNames ->
                possibleNames.any { name ->
                    lowercaseColumns.contains(name.lowercase())
                }
            }
            SchemaMatch(schema, matchedColumns, columnNames.size)
        }

        // Sort by: 1) match score (percentage), 2) matched columns (count), 3) prefer fewer total columns
        return matches
            .filter { it.matchedColumns > 0 } // Only consider schemas with at least one match
            .maxWithOrNull(
                compareBy<SchemaMatch> { it.matchScore }
                    .thenBy { it.matchedColumns }
                    .thenBy { -it.totalColumns },
            )?.schema ?: schemas.first()
    }

    fun parse(vaultId: String): List<Item> {
        return when (val bestSchema = findBestMatchingSchema()) {
            is Schema.Login -> parseLogin(vaultId, bestSchema)
            is Schema.SecureNote -> parseSecureNote(vaultId, bestSchema)
//            is Schema.PaymentCard -> parsePaymentCard(vaultId, bestSchema)
            is Schema.PaymentCard -> emptyList()
        }
    }

    private fun parseLogin(vaultId: String, schema: Schema.Login): List<Item> {
        val nameIndices = indexOf(schema.name)
        val urlIndices = indexOf(schema.url)
        val usernameIndices = indexOf(schema.username)
        val passwordIndices = indexOf(schema.password)
        val notesIndices = indexOf(schema.notes)

        return csvRecords.drop(1).map { record ->
            val itemUri = urlIndices.map { record[it].trim() }.firstOrNull { it.isNotEmpty() }?.let { url ->
                ItemUri(
                    text = url,
                    matcher = UriMatcher.Domain,
                )
            }

            Item.create(
                vaultId = vaultId,
                contentType = ItemContentType.Login,
                content = ItemContent.Login.Empty.copy(
                    name = nameIndices.map { record[it].trim() }.firstOrNull { it.isNotEmpty() }.orEmpty(),
                    username = usernameIndices.map { record[it].trim() }.firstOrNull { it.isNotEmpty() },
                    password = passwordIndices.map { record[it].trim() }.firstOrNull { it.isNotEmpty() }?.let { SecretField.ClearText(it) },
                    notes = notesIndices.map { record[it].trim() }.firstOrNull { it.isNotEmpty() },
                    iconType = IconType.Icon,
                    iconUriIndex = if (itemUri == null) null else 0,
                    uris = listOfNotNull(itemUri),
                ),
            )
        }
    }

    private fun parseSecureNote(vaultId: String, schema: Schema.SecureNote): List<Item> {
        val nameIndices = indexOf(schema.name)
        val textIndices = indexOf(schema.text)

        return csvRecords.drop(1).map { record ->
            val text = textIndices.map { record[it].trim() }.firstOrNull { it.isNotEmpty() }.orEmpty()

            Item.create(
                vaultId = vaultId,
                contentType = ItemContentType.SecureNote,
                content = ItemContent.SecureNote.Empty.copy(
                    name = nameIndices.map { record[it].trim() }.firstOrNull { it.isNotEmpty() }.orEmpty(),
                    text = if (text.isNotEmpty()) SecretField.ClearText(text) else null,
                ),
            )
        }
    }

    private fun parsePaymentCard(vaultId: String, schema: Schema.PaymentCard): List<Item> {
        val nameIndices = indexOf(schema.name)
        val cardholderIndices = indexOf(schema.cardholder)
        val numberIndices = indexOf(schema.number)
        val expirationIndices = indexOf(schema.expiration)
        val cvvIndices = indexOf(schema.cvv)
        val notesIndices = indexOf(schema.notes)

        return csvRecords.drop(1).map { record ->
            Item.create(
                vaultId = vaultId,
                contentType = ItemContentType.PaymentCard,
                content = ItemContent.PaymentCard.Empty.copy(
                    name = nameIndices.map { record[it].trim() }.firstOrNull { it.isNotEmpty() }.orEmpty(),
                    cardholder = cardholderIndices.map { record[it].trim() }.firstOrNull { it.isNotEmpty() },
                    number = numberIndices.map { record[it].trim() }.firstOrNull { it.isNotEmpty() }?.let { SecretField.ClearText(it) },
                    expiration = expirationIndices.map { record[it].trim() }.firstOrNull { it.isNotEmpty() },
                    cvv = cvvIndices.map { record[it].trim() }.firstOrNull { it.isNotEmpty() }?.let { SecretField.ClearText(it) },
                    notes = notesIndices.map { record[it].trim() }.firstOrNull { it.isNotEmpty() },
                ),
            )
        }
    }
}