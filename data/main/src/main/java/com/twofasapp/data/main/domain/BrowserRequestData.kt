/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.domain

import com.twofasapp.core.common.ktx.encodeByteArray
import com.twofasapp.core.common.ktx.encodeHex
import java.time.Instant

data class BrowserRequestData(
    val browser: ConnectedBrowser,
    val deviceId: String,
    val notificationId: String,
    val timestamp: Long,
    val pkPersBe: ByteArray,
    val pkEpheBe: ByteArray,
    val signature: ByteArray,
) {
    val sessionId: ByteArray
        get() = browser.nextSessionId

    val data: ByteArray
        get() = "${sessionId.encodeHex()}${deviceId}${pkEpheBe.encodeHex()}$timestamp".encodeByteArray()

    fun isExpired(now: Instant): Boolean {
        return Instant.ofEpochMilli(timestamp).isAfter(now.plusSeconds(2 * 60))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BrowserRequestData

        if (timestamp != other.timestamp) return false
        if (!sessionId.contentEquals(other.sessionId)) return false
        if (deviceId != other.deviceId) return false
        if (notificationId != other.notificationId) return false
        if (!pkPersBe.contentEquals(other.pkPersBe)) return false
        if (!pkEpheBe.contentEquals(other.pkEpheBe)) return false
        if (!signature.contentEquals(other.signature)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + sessionId.contentHashCode()
        result = 31 * result + deviceId.hashCode()
        result = 31 * result + notificationId.hashCode()
        result = 31 * result + pkPersBe.contentHashCode()
        result = 31 * result + pkEpheBe.contentHashCode()
        result = 31 * result + signature.contentHashCode()
        return result
    }
}