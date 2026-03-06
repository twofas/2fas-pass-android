package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.common.domain.items.ItemEncrypted
import com.twofasapp.data.main.VaultCipher

class ItemEncryptionMapperDelegate(
    private val loginEncryptionMapperStrategy: LoginEncryptionMapperStrategy,
    private val paymentCardEncryptionMapperStrategy: PaymentCardEncryptionMapperStrategy,
    private val secureNoteEncryptionMapperStrategy: SecureNoteEncryptionMapperStrategy,
    private val unknownEncryptionMapperStrategy: UnknownEncryptionMapperStrategy,
    private val wifiEncryptionMapperStrategy: WifiEncryptionMapperStrategy,
    private val passkeyEncryptionMapperStrategy: PasskeyEncryptionMapperStrategy
) : ItemEncryptionMapperStrategy<ItemContent> {

    override fun decryptItem(
        itemEncrypted: ItemEncrypted,
        vaultCipher: VaultCipher,
        decryptSecretFields: Boolean,
        contentEntityJson: String,
    ): ItemContent {
        return when (itemEncrypted.contentType) {
            ItemContentType.Login -> loginEncryptionMapperStrategy.decryptItem(
                itemEncrypted,
                vaultCipher,
                decryptSecretFields,
                contentEntityJson,
            )

            ItemContentType.PaymentCard -> paymentCardEncryptionMapperStrategy.decryptItem(
                itemEncrypted,
                vaultCipher,
                decryptSecretFields,
                contentEntityJson,
            )

            ItemContentType.SecureNote -> secureNoteEncryptionMapperStrategy.decryptItem(
                itemEncrypted,
                vaultCipher,
                decryptSecretFields,
                contentEntityJson,
            )

            is ItemContentType.Unknown -> unknownEncryptionMapperStrategy.decryptItem(
                itemEncrypted,
                vaultCipher,
                decryptSecretFields,
                contentEntityJson,
            )

            ItemContentType.Wifi -> wifiEncryptionMapperStrategy.decryptItem(
                itemEncrypted,
                vaultCipher,
                decryptSecretFields,
                contentEntityJson,
            )

            ItemContentType.Passkey -> passkeyEncryptionMapperStrategy.decryptItem(
                itemEncrypted,
                vaultCipher,
                decryptSecretFields,
                contentEntityJson,
            )
        }
    }

    override fun encryptItem(
        item: Item,
        content: ItemContent,
        vaultCipher: VaultCipher,
    ): String {
        return when (content) {
            is ItemContent.Login -> loginEncryptionMapperStrategy.encryptItem(
                item,
                content,
                vaultCipher,
            )

            is ItemContent.PaymentCard -> paymentCardEncryptionMapperStrategy.encryptItem(
                item,
                content,
                vaultCipher,
            )

            is ItemContent.SecureNote -> secureNoteEncryptionMapperStrategy.encryptItem(
                item,
                content,
                vaultCipher,
            )

            is ItemContent.Unknown -> unknownEncryptionMapperStrategy.encryptItem(
                item,
                content,
                vaultCipher,
            )

            is ItemContent.Wifi -> wifiEncryptionMapperStrategy.encryptItem(
                item,
                content,
                vaultCipher,
            )

            is ItemContent.Passkey -> passkeyEncryptionMapperStrategy.encryptItem(
                item,
                content,
                vaultCipher,
            )
        }
    }

    override fun decryptSecretFields(
        vaultCipher: VaultCipher,
        securityType: SecurityType,
        content: ItemContent,
    ): ItemContent {
        return when (content) {
            is ItemContent.Login -> loginEncryptionMapperStrategy.decryptSecretFields(
                vaultCipher,
                securityType,
                content,
            )

            is ItemContent.PaymentCard -> paymentCardEncryptionMapperStrategy.decryptSecretFields(
                vaultCipher,
                securityType,
                content,
            )

            is ItemContent.SecureNote -> secureNoteEncryptionMapperStrategy.decryptSecretFields(
                vaultCipher,
                securityType,
                content,
            )

            is ItemContent.Unknown -> unknownEncryptionMapperStrategy.decryptSecretFields(
                vaultCipher,
                securityType,
                content,
            )

            is ItemContent.Wifi -> wifiEncryptionMapperStrategy.decryptSecretFields(
                vaultCipher,
                securityType,
                content,
            )

            is ItemContent.Passkey -> passkeyEncryptionMapperStrategy.decryptSecretFields(
                vaultCipher,
                securityType,
                content,
            )
        }
    }

    override fun encryptSecretFields(
        content: ItemContent,
        encryptionKey: ByteArray,
    ): ItemContent {
        return when (content) {
            is ItemContent.Login -> loginEncryptionMapperStrategy.encryptSecretFields(
                content,
                encryptionKey,
            )

            is ItemContent.PaymentCard -> paymentCardEncryptionMapperStrategy.encryptSecretFields(
                content,
                encryptionKey,
            )

            is ItemContent.SecureNote -> secureNoteEncryptionMapperStrategy.encryptSecretFields(
                content,
                encryptionKey,
            )

            is ItemContent.Unknown -> unknownEncryptionMapperStrategy.encryptSecretFields(
                content,
                encryptionKey,
            )

            is ItemContent.Wifi -> wifiEncryptionMapperStrategy.encryptSecretFields(
                content,
                encryptionKey,
            )

            is ItemContent.Passkey -> passkeyEncryptionMapperStrategy.encryptSecretFields(
                content,
                encryptionKey,
            )
        }
    }
}