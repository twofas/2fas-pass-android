/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.ktx

import java.util.regex.Pattern.compile

private val emailRegex = compile(
    "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
        "\\@" +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(" +
        "\\." +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
        ")+",
)

private val urlRegex = compile(
    "(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})\n",
)

fun String?.isEmail(): Boolean {
    return this?.let { emailRegex.matcher(it).matches() } ?: false
}

fun String?.isUrl(): Boolean {
    return this?.let { urlRegex.matcher(it).matches() } ?: false
}

inline fun String?.ifNullOrEmpty(defaultValue: () -> String): String =
    if (isNullOrEmpty()) defaultValue() else this

inline fun String?.ifNullOrBlank(defaultValue: () -> String): String =
    if (isNullOrBlank()) defaultValue() else this

inline fun <reified T : Enum<*>> enumValueOrNull(name: String?): T? =
    T::class.java.enumConstants?.firstOrNull { it.name == name }

fun <T : Enum<*>> enumValueOrNull(cls: Class<T>, name: String?): T? {
    return cls.enumConstants?.firstOrNull { it.name == name }
}

fun String?.indexesOf(substring: String, ignoreCase: Boolean = false): List<Int> {
    return this?.let {
        val regex = if (ignoreCase) Regex("(?=$substring)", RegexOption.IGNORE_CASE) else Regex("(?=$substring)")
        regex.findAll(this).map { it.range.first }.toList()
    } ?: emptyList()
}

fun String.splitAndMatch(substring: String, ignoreCase: Boolean = false): List<Pair<String, Boolean>> {
    if (substring.isBlank()) return listOf(this to false)
    if (!contains(substring, ignoreCase)) return listOf(this to false)

    val result = mutableListOf<Pair<String, Boolean>>()
    var currentIndex = 0

    while (true) {
        val foundIndex = indexOf(substring, currentIndex, ignoreCase)
        if (foundIndex == -1) break

        if (currentIndex < foundIndex) {
            result.add(substring(currentIndex, foundIndex) to false)
        }

        result.add(substring(foundIndex, foundIndex + substring.length) to true)
        currentIndex = foundIndex + substring.length
    }

    if (currentIndex < this.length) {
        result.add(substring(currentIndex, this.length) to false)
    }

    return result
}

fun String.removeWhitespace(): String {
    return filterNot { it.isWhitespace() }
}