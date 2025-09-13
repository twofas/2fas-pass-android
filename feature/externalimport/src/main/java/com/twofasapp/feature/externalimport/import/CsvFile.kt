/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.externalimport.import

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import com.twofasapp.core.common.domain.IconType
import com.twofasapp.core.common.domain.ItemUri
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.UriMatcher
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import java.io.StringReader

internal data class CsvFile(
    val text: String,
    val delimiter: Char,
    val schema: Schema,
) {
    private val csvRecords: List<List<String>> by lazy {
        val csvReader = CSVReaderBuilder(StringReader(text))
            .withCSVParser(
                CSVParserBuilder()
                    .withSeparator(delimiter)
                    .withQuoteChar('"')
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

    data class Schema(
        val name: List<String>,
        val url: List<String>,
        val username: List<String>,
        val password: List<String>,
        val notes: List<String>,
    )

    private fun indexOf(columnNames: List<String>): List<Int> {
        val lowercaseColumns = csvRecords.first().map { it.lowercase() }
        val indices = columnNames.mapNotNull { columnName ->
            val index = lowercaseColumns.indexOf(columnName.lowercase())
            if (index != -1) index else null
        }

        return indices
    }

    fun parse(vaultId: String): List<Item> {
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
                contentType = "login",
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
}