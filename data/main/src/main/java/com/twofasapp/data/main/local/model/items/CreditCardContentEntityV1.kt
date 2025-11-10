package com.twofasapp.data.main.local.model.items

import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.serializers.EncryptedBytesSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreditCardContentEntityV1(
    @SerialName("name")
    val name: String,
    @SerialName("cardholder")
    val cardholder: String?,
    @SerialName("s_number")
    @Serializable(with = EncryptedBytesSerializer::class)
    val number: EncryptedBytes?,
    @SerialName("expiration")
    val expiration: String?,
    @SerialName("s_cvv")
    @Serializable(with = EncryptedBytesSerializer::class)
    val cvv: EncryptedBytes?,
    @SerialName("notes")
    val notes: String?,
)