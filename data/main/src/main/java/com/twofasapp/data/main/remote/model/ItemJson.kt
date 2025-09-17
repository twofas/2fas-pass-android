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
data class ItemJson(
    @SerialName("id")
    val id: String,
    @SerialName("deviceId")
    val deviceId: String? = null,
    @SerialName("createdAt")
    val createdAt: Long,
    @SerialName("updatedAt")
    val updatedAt: Long,
    @SerialName("securityType")
    val securityType: Int,
    @SerialName("contentType")
    val contentType: String,
    @SerialName("contentVersion")
    val contentVersion: Int,
    @SerialName("content")
    val content: ItemContentJson?,
    @SerialName("tags")
    val tags: List<String>?,
)