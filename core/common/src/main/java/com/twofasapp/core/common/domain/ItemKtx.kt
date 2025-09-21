package com.twofasapp.core.common.domain

import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent

fun Item.normalizeBeforeSaving(): Item {
    val normalizedContent = when (content) {
        is ItemContent.Unknown -> content
        is ItemContent.Login -> content.filterAndNormalizeUris()
        is ItemContent.SecureNote -> content
    }

    return copy(content = normalizedContent)
}

private fun ItemContent.Login.filterAndNormalizeUris(): ItemContent.Login {
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