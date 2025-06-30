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
internal sealed interface OutgoingPayloadJson {
    @Serializable
    data class Hello(
        val deviceId: String,
        val deviceName: String,
        val deviceOs: String,
    ) : OutgoingPayloadJson

    @Serializable
    data class CloseWithError(
        val errorCode: Int,
        val errorMessage: String,
    ) : OutgoingPayloadJson

    @Serializable
    data class Challenge(
        val pkEpheMa: String,
        val hkdfSalt: String,
    ) : OutgoingPayloadJson

    @Serializable
    data class InitTransfer(
        val totalChunks: Int,
        val totalSize: Int,
        val sha256GzipVaultDataEnc: String,
        val fcmTokenEnc: String,
        val newSessionIdEnc: String,
        val expirationDateEnc: String?,
    ) : OutgoingPayloadJson

    @Serializable
    data class TransferChunk(
        val chunkIndex: Int,
        val chunkSize: Int,
        val chunkData: String,
    ) : OutgoingPayloadJson

    @Serializable
    data class PullRequest(
        val newSessionIdEnc: String,
    ) : OutgoingPayloadJson

    @Serializable
    data class PullRequestAction(
        val dataEnc: String,
    ) : OutgoingPayloadJson

    @Serializable
    data object CloseWithSuccess : OutgoingPayloadJson
}