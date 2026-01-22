package com.twofasapp.core.design.feature.items

import com.twofasapp.core.common.domain.IconType
import com.twofasapp.core.common.domain.ItemUri
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.UriMatcher
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.design.foundation.preview.PreviewTextLong

fun itemPreview(content: ItemContent): Item {
    return Item.Empty.copy(content = content)
}

val LoginItemContentPreview: ItemContent.Login = ItemContent.Login(
    name = "Login Name",
    username = "user@mail.com",
    password = SecretField.ClearText(""),
    uris = listOf(
        ItemUri("https://2fas.com", UriMatcher.Domain),
        ItemUri("https://google.com", UriMatcher.Host),
    ),
    iconType = IconType.Label,
    iconUriIndex = 0,
    customImageUrl = null,
    labelText = "NA",
    labelColor = "#FF55FF",
    notes = null,
)

val SecureNoteItemContentPreview: ItemContent.SecureNote = ItemContent.SecureNote(
    name = "Secure Note Name",
    text = SecretField.ClearText(PreviewTextLong),
    additionalInfo = null,
)

val LoginItemPreview = itemPreview(LoginItemContentPreview)
val SecureNoteItemPreview = itemPreview(SecureNoteItemContentPreview)