package com.twofasapp.data.main.websocket.messages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ActionFieldJson(
    @SerialName("value")
    val value: String,
    @SerialName("action")
    val action: String,
)