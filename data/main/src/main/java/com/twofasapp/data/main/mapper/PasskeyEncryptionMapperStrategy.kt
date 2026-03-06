package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.crypto.encrypt
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.clearText
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemEncrypted
import com.twofasapp.data.main.VaultCipher
import com.twofasapp.data.main.local.model.items.PasskeyContentEntityV1
import kotlinx.serialization.json.Json

class PasskeyEncryptionMapperStrategy(
    private val json: Json,
) : ItemEncryptionMapperStrategy<ItemContent.Passkey> {

    override fun decryptItem(
        itemEncrypted: ItemEncrypted,
        vaultCipher: VaultCipher,
        decryptSecretFields: Boolean,
        contentEntityJson: String,
    ): ItemContent.Passkey {
        val contentEntity =
            json.decodeFromString(PasskeyContentEntityV1.serializer(), contentEntityJson)
        return ItemContent.Passkey(
            name = contentEntity.name,
            privateKey = contentEntity.privateKey?.let {
                if (decryptSecretFields) {
                    SecretField.ClearText(
                        when (itemEncrypted.securityType) {
                            SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(it)
                            SecurityType.Tier2 -> vaultCipher.decryptWithSecretKey(it)
                            SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(it)
                        },
                    )
                } else {
                    SecretField.Encrypted(it)
                }
            },
            userHandle = contentEntity.userHandle,
            credentialId = contentEntity.credentialId,
            rpId = contentEntity.rpId,
            notes = contentEntity.notes,
        )
    }

    override fun encryptItem(
        item: Item,
        content: ItemContent.Passkey,
        vaultCipher: VaultCipher,
    ): String {
        return json.encodeToString(
            PasskeyContentEntityV1(
                name = content.name,
                privateKey = when (content.privateKey) {
                    is SecretField.Encrypted -> (content.privateKey as SecretField.Encrypted).value
                    is SecretField.ClearText -> {
                        if (content.privateKey.clearText.isBlank()) {
                            null
                        } else {
                            when (item.securityType) {
                                SecurityType.Tier1 -> vaultCipher.encryptWithSecretKey(content.privateKey.clearText)
                                SecurityType.Tier2 -> vaultCipher.encryptWithSecretKey(content.privateKey.clearText)
                                SecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey(content.privateKey.clearText)
                            }
                        }
                    }

                    null -> null
                },
                userHandle = content.userHandle,
                credentialId = content.credentialId,
                rpId = content.rpId,
                notes = content.notes,
            ),
        )
    }

    override fun decryptSecretFields(
        vaultCipher: VaultCipher,
        securityType: SecurityType,
        content: ItemContent.Passkey,
    ): ItemContent.Passkey {
        return content.copy(
            privateKey = content.privateKey?.let {
                when (it) {
                    is SecretField.ClearText -> it
                    is SecretField.Encrypted -> {
                        SecretField.ClearText(
                            when (securityType) {
                                SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(it.value)
                                SecurityType.Tier2 -> vaultCipher.decryptWithSecretKey(it.value)
                                SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(it.value)
                            },
                        )
                    }
                }
            },
        )
    }

    override fun encryptSecretFields(
        content: ItemContent.Passkey,
        encryptionKey: ByteArray,
    ): ItemContent.Passkey {
        return content.copy(
            privateKey = content.privateKey?.let {
                when (it) {
                    is SecretField.Encrypted -> it
                    is SecretField.ClearText -> {
                        if (it.value.isBlank()) {
                            null
                        } else {
                            SecretField.Encrypted(
                                encrypt(key = encryptionKey, data = it.value),
                            )
                        }
                    }
                }
            },
        )
    }
}