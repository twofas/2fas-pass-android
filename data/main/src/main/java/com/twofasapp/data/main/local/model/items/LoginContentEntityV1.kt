package com.twofasapp.data.main.local.model.items

import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.serializers.EncryptedBytesSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginContentEntityV1(
    @SerialName("name")
    val name: String,
    @SerialName("username")
    val username: String?,
    @SerialName("password")
    @Serializable(with = EncryptedBytesSerializer::class)
    val password: EncryptedBytes?,
    @SerialName("uris")
    val uris: List<UriJson>,
    @SerialName("iconType")
    val iconType: Int,
    @SerialName("iconUriIndex")
    val iconUriIndex: Int?,
    @SerialName("labelText")
    val labelText: String?,
    @SerialName("labelColor")
    val labelColor: String?,
    @SerialName("customImageUrl")
    val customImageUrl: String?,
    @SerialName("notes")
    val notes: String?,
) : ContentEntity {

    @Serializable
    data class UriJson(
        @SerialName("text")
        val text: String,
        @SerialName("matcher")
        val matcher: Int,
    )
}