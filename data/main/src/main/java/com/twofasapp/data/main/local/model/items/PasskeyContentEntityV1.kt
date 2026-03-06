package com.twofasapp.data.main.local.model.items

import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.serializers.EncryptedBytesSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PasskeyContentEntityV1(
    @SerialName("name")
    val name: String,
    @SerialName("s_privateKey")
    @Serializable(with = EncryptedBytesSerializer::class)
    val privateKey: EncryptedBytes?,
    @SerialName("userHandle")
    val userHandle: String?,
    @SerialName("credentialId")
    val credentialId: String?,
    @SerialName("rpId")
    val rpId: String?,
    @SerialName("notes")
    val notes: String?,
)