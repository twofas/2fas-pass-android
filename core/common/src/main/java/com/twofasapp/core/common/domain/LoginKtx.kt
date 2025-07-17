package com.twofasapp.core.common.domain

fun Login.filterAndNormalizeUris(): Login {
    if (uris.size == 1) {
        return this
    }

    val normalizedUris = if (uris.all { it.text.isBlank() }) {
        uris.take(1)
    } else {
        uris.filter { it.text.isNotBlank() }
    }.map { it.copy(text = it.text.trim()) }

    return copy(
        uris = normalizedUris,
        iconUriIndex = iconUriIndex?.let { minOf(it, normalizedUris.size - 1) },
    )
}