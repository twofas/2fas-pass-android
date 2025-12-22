package com.twofasapp.feature.externalimport.import

import com.opencsv.CSVParser
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import java.io.StringReader

internal object CsvParser {
    fun parse(
        text: String,
        delimiter: Char = ',',
        readRow: (row: Map<String, String>) -> Unit,
    ) {
        val csvReader = CSVReaderBuilder(StringReader(text))
            .withCSVParser(
                CSVParserBuilder()
                    .withSeparator(delimiter)
                    .withQuoteChar('"')
                    .withEscapeChar(CSVParser.NULL_CHARACTER)
                    .build(),
            )
            .build()

        csvReader.use { reader ->
            // Read header row
            val headers = reader.readNext()?.map { it.trim().lowercase() } ?: return

            // Read data rows until EOF
            while (true) {
                val row = reader.readNext() ?: break

                // Skip completely empty rows
                if (row.isEmpty()) continue

                // Skip rows with no actual values (all fields blank)
                if (row.all { it.isBlank() }) continue

                val values = row.toList().map { it.trim() }

                // Normalize row length to match headers count
                val normalizedValues = when {
                    values.size < headers.size -> values + List(headers.size - values.size) { "" }
                    values.size > headers.size -> values.take(headers.size)
                    else -> values
                }

                // Map column names to values
                val rowMap = headers.zip(normalizedValues).toMap()

                readRow(rowMap)
            }
        }
    }
}