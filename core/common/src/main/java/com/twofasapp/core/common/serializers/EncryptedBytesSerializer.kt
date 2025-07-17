package com.twofasapp.core.common.serializers

import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.core.common.ktx.encodeBase64
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
object EncryptedBytesSerializer : KSerializer<EncryptedBytes?> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("EncryptedBytes", PrimitiveKind.STRING).nullable

    override fun serialize(encoder: Encoder, value: EncryptedBytes?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeString(value.bytes.encodeBase64())
        }
    }

    override fun deserialize(decoder: Decoder): EncryptedBytes? {
        return if (decoder.decodeNotNullMark()) {
            EncryptedBytes(decoder.decodeString().decodeBase64())
        } else {
            decoder.decodeNull()
            null
        }
    }
}