/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.domain

import com.twofasapp.core.common.ktx.decodeHex
import com.twofasapp.core.common.ktx.encodeByteArray

data class ConnectData(
    val version: Int,
    val sessionId: String,
    private val pkPersBeHex: String,
    private val pkEpheBeHex: String,
    private val signatureHex: String,
) {
    companion object {
        const val CurrentSchema = 1
    }

    val data: ByteArray
        get() = "${sessionId}${pkPersBeHex}$pkEpheBeHex".encodeByteArray()

    val pkPersBe: ByteArray = pkPersBeHex.decodeHex()
    val pkEpheBe: ByteArray = pkEpheBeHex.decodeHex()
    val signature: ByteArray = signatureHex.decodeHex()
}