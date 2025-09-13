package com.twofasapp.core.common.domain.items

import com.twofasapp.core.common.domain.IconType
import com.twofasapp.core.common.domain.ItemUri
import com.twofasapp.core.common.domain.SecretField

sealed interface ItemContent {
    val name: String

    val defaultLabelText: String
        get() = name.trim().take(2).uppercase()

    data class Unknown private constructor(
        override val name: String,
        val rawJson: String,
    ) : ItemContent {
        constructor(rawJson: String) : this(name = "", rawJson = rawJson)
    }

    data class Login(
        override val name: String,
        val username: String?,
        val password: SecretField?,
        val uris: List<ItemUri>,
        val iconType: IconType,
        val iconUriIndex: Int?,
        val customImageUrl: String?,
        val labelText: String?,
        val labelColor: String?,
        val notes: String?,
    ) : ItemContent {

        companion object {
            val Empty = Login(
                name = "",
                username = null,
                password = SecretField.ClearText(""),
                uris = emptyList(),
                iconType = IconType.Icon,
                iconUriIndex = 0,
                customImageUrl = null,
                labelText = null,
                labelColor = null,
                notes = null,
            )
        }

        val iconUrl: String?
            get() = iconUriIndex?.let { uris.getOrNull(it)?.iconUrl }
    }

    data class SecureNote(
        override val name: String,
        val text: String?,
    ) : ItemContent
}