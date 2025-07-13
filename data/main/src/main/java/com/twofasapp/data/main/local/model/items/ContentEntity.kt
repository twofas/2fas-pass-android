package com.twofasapp.data.main.local.model.items

internal sealed interface ContentEntity {
    val contentType: String
    val contentVersion: Int
}