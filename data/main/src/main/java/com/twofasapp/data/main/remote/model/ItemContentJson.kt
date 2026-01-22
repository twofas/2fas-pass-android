/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface ItemContentJson {

    @Serializable
    data class Login(
        @SerialName("name")
        val name: String?,
        @SerialName("username")
        val username: String?,
        @SerialName("s_password")
        val password: String?,
        @SerialName("uris")
        val uris: List<UriJson>,
        @SerialName("iconType")
        val iconType: Int,
        @SerialName("iconUriIndex")
        val iconUriIndex: Int?,
        @SerialName("labelText")
        val labelText: String?,
        @SerialName("labelColor")
        val labelColor: String?,
        @SerialName("customImageUrl")
        val customImageUrl: String?,
        @SerialName("notes")
        val notes: String?,
    ) : ItemContentJson {
        @Serializable
        data class UriJson(
            @SerialName("text")
            val text: String,
            @SerialName("matcher")
            val matcher: Int,
        )
    }

    @Serializable
    data class SecureNote(
        @SerialName("name")
        val name: String?,
        @SerialName("s_text")
        val text: String?,
        @SerialName("additionalInfo")
        val additionalInfo: String?,
    ) : ItemContentJson

    @Serializable
    data class PaymentCard(
        @SerialName("name")
        val name: String?,
        @SerialName("cardHolder")
        val cardHolder: String?,
        @SerialName("s_cardNumber")
        val cardNumber: String?,
        @SerialName("s_expirationDate")
        val expirationDate: String?,
        @SerialName("s_securityCode")
        val securityCode: String?,
        @SerialName("cardNumberMask")
        val cardNumberMask: String?,
        @SerialName("cardIssuer")
        val cardIssuer: String?,
        @SerialName("notes")
        val notes: String?,
    ) : ItemContentJson
}