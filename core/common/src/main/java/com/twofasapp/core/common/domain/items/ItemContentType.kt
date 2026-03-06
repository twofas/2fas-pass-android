package com.twofasapp.core.common.domain.items

sealed interface ItemContentType {

    val key: String
    val version: Int
    val fillable: Boolean

    companion object {
        fun fromKey(key: String): ItemContentType {
            return values().firstOrNull { it.key == key } ?: Unknown(key)
        }

        fun values(): List<ItemContentType> {
            return listOf(
                Login,
                PaymentCard,
                SecureNote,
                Wifi,
                Passkey
            )
        }
    }

    object Login : ItemContentType {
        override val key: String = "login"
        override val version: Int = 1
        override val fillable: Boolean = true
    }

    object SecureNote : ItemContentType {
        override val key: String = "secureNote"
        override val version: Int = 1
        override val fillable: Boolean = false
    }

    object PaymentCard : ItemContentType {
        override val key: String = "paymentCard"
        override val version: Int = 1
        override val fillable: Boolean = false
    }

    object Wifi : ItemContentType {
        override val key: String = "wifi"
        override val version: Int = 1
        override val fillable: Boolean = false
    }

    object Passkey : ItemContentType {
        override val key: String = "passkey"
        override val version: Int = 1
        override val fillable: Boolean = false
    }

    data class Unknown(
        override val key: String,
        override val version: Int = 1,
    ) : ItemContentType {
        override val fillable: Boolean = false
    }
}