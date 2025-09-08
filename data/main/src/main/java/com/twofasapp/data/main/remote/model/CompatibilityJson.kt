package com.twofasapp.data.main.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CompatibilityJson(
    @SerialName("minimalAndroidVersion")
    val minimalAndroidVersion: Int?,
)