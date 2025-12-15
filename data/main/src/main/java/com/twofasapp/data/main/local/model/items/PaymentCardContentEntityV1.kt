package com.twofasapp.data.main.local.model.items

import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.serializers.EncryptedBytesSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentCardContentEntityV1(
    @SerialName("name")
    val name: String,
    @SerialName("cardHolder")
    val cardHolder: String?,
    @SerialName("s_cardNumber")
    @Serializable(with = EncryptedBytesSerializer::class)
    val cardNumber: EncryptedBytes?,
    @SerialName("s_expirationDate")
    @Serializable(with = EncryptedBytesSerializer::class)
    val expirationDate: EncryptedBytes?,
    @SerialName("s_securityCode")
    @Serializable(with = EncryptedBytesSerializer::class)
    val securityCode: EncryptedBytes?,
    @SerialName("cardNumberMask")
    val cardNumberMask: String?,
    @SerialName("cardIssuer")
    val cardIssuer: String?,
    @SerialName("notes")
    val notes: String?,
)