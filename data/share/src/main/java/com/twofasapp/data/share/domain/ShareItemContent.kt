package com.twofasapp.data.share.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface ShareItemContent {

    @Serializable
    data class Login(
        @SerialName("name")
        val name: String,
        @SerialName("username")
        val username: String?,
        @SerialName("password")
        val password: String?,
        @SerialName("notes")
        val notes: String?,
        @SerialName("uris")
        val uris: List<Uri>?,
    ) : ShareItemContent {

        @Serializable
        data class Uri(
            @SerialName("text")
            val text: String,
            @SerialName("matcher")
            val matcher: Int?,
        )
    }

    @Serializable
    data class SecureNote(
        @SerialName("name")
        val name: String,
        @SerialName("text")
        val text: String?,
    ) : ShareItemContent

    @Serializable
    data class PaymentCard(
        @SerialName("name")
        val name: String,
        @SerialName("cardHolder")
        val cardHolder: String?,
        @SerialName("cardNumber")
        val cardNumber: String?,
        @SerialName("expirationDate")
        val expirationDate: String?,
        @SerialName("securityCode")
        val securityCode: String?,
        @SerialName("notes")
        val notes: String?,
    ) : ShareItemContent

    @Serializable
    data class Wifi(
        @SerialName("name")
        val name: String,
        @SerialName("ssid")
        val ssid: String?,
        @SerialName("password")
        val password: String?,
        @SerialName("notes")
        val notes: String?,
        @SerialName("securityType")
        val securityType: String?,
        @SerialName("hidden")
        val hidden: Boolean?,
    ) : ShareItemContent

    @Serializable
    data class Custom(
        @SerialName("text")
        val text: String,
    ) : ShareItemContent
}