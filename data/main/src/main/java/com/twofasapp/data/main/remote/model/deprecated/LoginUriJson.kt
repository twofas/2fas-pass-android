package com.twofasapp.data.main.remote.model.deprecated

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginUriJson(
    @SerialName("text")
    val text: String,
    @SerialName("matcher")
    val matcher: Int,
)