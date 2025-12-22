package com.twofasapp.feature.externalimport.import

internal class CsvRow(
    private val map: Map<String, String>
) {
    fun get(key: String): String? {
        return map[key.trim().lowercase()]?.ifBlank { null }
    }
}
