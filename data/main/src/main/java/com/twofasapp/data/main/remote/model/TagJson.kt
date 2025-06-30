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
data class TagJson(
    @SerialName("id")
    val id: String,
    @SerialName("updatedAt")
    val updatedAt: Long,
    @SerialName("name")
    val name: String,
    @SerialName("color")
    val color: String?,
    @SerialName("position")
    val position: Int,
)