/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.websocket.messages

import kotlinx.serialization.Serializable

@Serializable
internal data class OutgoingMessageJson(
    val scheme: Int,
    val origin: String,
    val originVersion: String,
    val id: String,
    val action: String,
    val payload: OutgoingPayloadJson? = null,
)