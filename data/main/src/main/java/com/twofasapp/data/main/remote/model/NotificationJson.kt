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
internal data class NotificationJson(
    @SerialName("id")
    val id: String,
    @SerialName("data")
    val data: Data,
) {
    @Serializable
    data class Data(
        @SerialName("messageType")
        val messageType: String,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("pkPersBe")
        val pkPersBe: String,
        @SerialName("pkEpheBe")
        val pkEpheBe: String,
        @SerialName("sigPush")
        val sigPush: String,
    )
}