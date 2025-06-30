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

@Serializable
data class LoginJson(
    @SerialName("id")
    val id: String,
    @SerialName("deviceId")
    val deviceId: String? = null,
    @SerialName("createdAt")
    val createdAt: Long,
    @SerialName("updatedAt")
    val updatedAt: Long,
    @SerialName("name")
    val name: String,
    @SerialName("username")
    val username: String?,
    @SerialName("password")
    val password: String?,
    @SerialName("securityType")
    val securityType: Int,
    @SerialName("uris")
    val uris: List<LoginUriJson>,
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
    @SerialName("tags")
    val tags: List<String>?,
)