package com.twofasapp.feature.externalimport.import

internal object TransferUtils {

    private val camelCaseRegex = Regex("([a-z])([A-Z])")
    private val letterDigitRegex = Regex("([a-zA-Z])([0-9])")
    private val digitLetterRegex = Regex("([0-9])([a-zA-Z])")

    private fun String.formatAsLabel(): String =
        replace("_", " ")
            .replace(camelCaseRegex, "$1 $2")
            .replace(letterDigitRegex, "$1 $2")
            .replace(digitLetterRegex, "$1 $2")
            .replaceFirstChar { it.uppercase() }

    private fun Any?.asNonBlankStringOrNull(): String? = when (this) {
        null -> null
        is String -> trim().takeIf { it.isNotBlank() }
        is Int, is Double, is Float, is Boolean -> toString()
        else -> null
    }

    fun formatNote(
        note: String?,
        fields: Map<String, Any?> = emptyMap(),
    ): String? {
        val baseNote = note?.trim()?.takeIf { it.isNotBlank() }

        val extra = fields
            .asSequence()
            .mapNotNull { (key, value) ->
                val v = value.asNonBlankStringOrNull() ?: return@mapNotNull null
                "${key.formatAsLabel()}: $v"
            }
            .sorted()
            .joinToString("\n")
            .ifBlank { null }

        return when {
            baseNote != null && extra != null -> "$baseNote\n\n$extra"
            baseNote != null -> baseNote
            else -> extra
        }
    }
}