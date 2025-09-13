package com.twofasapp.data.main.local.model.items

import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.serializers.EncryptedBytesSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SecureNoteContentEntityV1(
    @SerialName("name")
    val name: String,
    @SerialName("text")
    @Serializable(with = EncryptedBytesSerializer::class)
    val text: EncryptedBytes?,
) : ContentEntity {
    override val contentType: String = "secureNote"
    override val contentVersion: Int = 1
}