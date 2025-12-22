package com.twofasapp.core.common.domain.items

import com.twofasapp.core.common.domain.IconType
import com.twofasapp.core.common.domain.ItemUri
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.UriMatcher

sealed interface ItemContent {
    val name: String

    val defaultLabelText: String
        get() = name.trim().take(2).uppercase()

    data class Unknown private constructor(
        override val name: String,
        val rawJson: String,
    ) : ItemContent {
        constructor(rawJson: String) : this(name = "", rawJson = rawJson)
    }

    data class Login(
        override val name: String,
        val username: String?,
        val password: SecretField?,
        val uris: List<ItemUri>,
        val iconType: IconType,
        val iconUriIndex: Int?,
        val customImageUrl: String?,
        val labelText: String?,
        val labelColor: String?,
        val notes: String?,
    ) : ItemContent {

        companion object {
            val Empty = Login(
                name = "",
                username = null,
                password = null,
                uris = emptyList(),
                iconType = IconType.Icon,
                iconUriIndex = 0,
                customImageUrl = null,
                labelText = null,
                labelColor = null,
                notes = null,
            )

            fun create(
                name: String? = null,
                username: String? = null,
                password: String? = null,
                url: String? = null,
                notes: String? = null,
            ): Login {
                val itemUri = url?.let { url ->
                    ItemUri(
                        text = url,
                        matcher = UriMatcher.Domain,
                    )
                }

                return Login(
                    name = name.orEmpty(),
                    username = username,
                    password = password?.let { SecretField.ClearText(it) },
                    iconType = IconType.Icon,
                    iconUriIndex = if (itemUri == null) null else 0,
                    uris = listOfNotNull(itemUri),
                    customImageUrl = null,
                    labelText = null,
                    labelColor = null,
                    notes = notes,
                )
            }
        }

        val iconUrl: String?
            get() = iconUriIndex?.let { uris.getOrNull(it)?.iconUrl }
    }

    data class SecureNote(
        override val name: String,
        val text: SecretField?,
    ) : ItemContent {
        companion object {
            val Empty = SecureNote(
                name = "",
                text = null,
            )

            val Limit: Int = 16_384
        }
    }

    data class PaymentCard(
        override val name: String,
        val cardHolder: String?,
        val cardNumber: SecretField?,
        val cardNumberMask: String?,
        val expirationDate: SecretField?,
        val securityCode: SecretField?,
        val cardIssuer: Issuer?,
        val notes: String?,
    ) : ItemContent {

        enum class Issuer(val code: String) {
            Visa("Visa"),
            MasterCard("MC"),
            AmericanExpress("AMEX"),
            Discover("Discover"),
            DinersClub("DinersClub"),
            Jcb("JCB"),
            UnionPay("UnionPay"),
            ;

            companion object {
                fun fromCode(code: String?): Issuer? {
                    return entries.find { it.code == code }
                }
            }
        }

        companion object {
            val Empty = PaymentCard(
                name = "",
                cardHolder = null,
                cardNumber = null,
                cardNumberMask = null,
                expirationDate = null,
                securityCode = null,
                cardIssuer = null,
                notes = null,
            )
        }
    }
}