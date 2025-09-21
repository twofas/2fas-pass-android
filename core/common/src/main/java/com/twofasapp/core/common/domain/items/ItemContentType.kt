package com.twofasapp.core.common.domain.items

sealed interface ItemContentType {
    val key: String
    val version: Int
    val fillable: Boolean

    companion object {
        fun fromKey(key: String): ItemContentType {
            return when (key) {
                Login.key -> Login
                SecureNote.key -> SecureNote
                else -> Unknown(key = key)
            }
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

    data class Unknown(
        override val key: String,
        override val version: Int = 1,
    ) : ItemContentType {
        override val fillable: Boolean = false
    }
}