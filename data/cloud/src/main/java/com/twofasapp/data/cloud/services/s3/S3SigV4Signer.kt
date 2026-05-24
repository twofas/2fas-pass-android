/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.services.s3

import com.twofasapp.data.cloud.domain.CloudConfig
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal object S3SigV4Signer {

    const val EMPTY_BODY_SHA256_HEX = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"

    fun sha256Hex(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(data)
        return digest.toHexString()
    }

    data class SignedHeaders(val headers: Map<String, String>)

    fun sign(
        method: String,
        url: String,
        host: String,
        queryString: String,
        canonicalPath: String,
        headersToSign: Map<String, String>,
        bodySha256Hex: String,
        now: Date,
        config: CloudConfig.S3,
    ): SignedHeaders {
        val amzDate = amzDate(now)
        val dateStamp = amzDate.substring(0, 8)
        val service = "s3"

        val mutableHeaders = headersToSign.toMutableMap()
        mutableHeaders["Host"] = host
        mutableHeaders["x-amz-date"] = amzDate
        mutableHeaders["x-amz-content-sha256"] = bodySha256Hex

        val (canonicalHeaders, signedHeaders) = canonicalHeaders(mutableHeaders)

        val canonicalRequest = listOf(
            method,
            canonicalPath.ifEmpty { "/" },
            queryString,
            canonicalHeaders,
            signedHeaders,
            bodySha256Hex,
        ).joinToString("\n")

        val credentialScope = "$dateStamp/${config.region}/$service/aws4_request"
        val stringToSign = listOf(
            "AWS4-HMAC-SHA256",
            amzDate,
            credentialScope,
            sha256Hex(canonicalRequest.toByteArray(Charsets.UTF_8)),
        ).joinToString("\n")

        val kDate = hmac("AWS4${config.secretAccessKey}".toByteArray(Charsets.UTF_8), dateStamp)
        val kRegion = hmac(kDate, config.region)
        val kService = hmac(kRegion, service)
        val kSigning = hmac(kService, "aws4_request")
        val signature = hmac(kSigning, stringToSign).toHexString()

        val authorization = "AWS4-HMAC-SHA256 " +
            "Credential=${config.accessKeyId}/$credentialScope," +
            "SignedHeaders=$signedHeaders," +
            "Signature=$signature"

        mutableHeaders["Authorization"] = authorization
        return SignedHeaders(mutableHeaders)
    }

    fun percentEncodePathSegment(segment: String): String {
        val sb = StringBuilder()
        for (byte in segment.toByteArray(Charsets.UTF_8)) {
            val b = byte.toInt() and 0xFF
            val isUnreserved = (b in 'A'.code..'Z'.code) ||
                (b in 'a'.code..'z'.code) ||
                (b in '0'.code..'9'.code) ||
                b == '-'.code ||
                b == '_'.code ||
                b == '.'.code ||
                b == '~'.code
            if (isUnreserved) {
                sb.append(b.toChar())
            } else {
                sb.append('%')
                sb.append(String.format("%02X", b))
            }
        }
        return sb.toString()
    }

    private fun amzDate(date: Date): String {
        val formatter = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(date)
    }

    private fun canonicalHeaders(headers: Map<String, String>): Pair<String, String> {
        val filtered = headers.filter { (key, _) ->
            val lowered = key.lowercase(Locale.US)
            lowered == "host" || lowered == "content-type" || lowered.startsWith("x-amz-")
        }
        val normalized = filtered
            .map { (key, value) -> key.lowercase(Locale.US) to collapseWhitespace(value) }
            .sortedBy { it.first }
        val canonical = normalized.joinToString("") { "${it.first}:${it.second}\n" }
        val signed = normalized.joinToString(";") { it.first }
        return canonical to signed
    }

    private fun collapseWhitespace(input: String): String {
        val trimmed = input.trim()
        val sb = StringBuilder()
        var previousSpace = false
        for (c in trimmed) {
            if (c == ' ') {
                if (!previousSpace) sb.append(c)
                previousSpace = true
            } else {
                sb.append(c)
                previousSpace = false
            }
        }
        return sb.toString()
    }

    private fun hmac(key: ByteArray, message: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(message.toByteArray(Charsets.UTF_8))
    }

    private fun ByteArray.toHexString(): String {
        val sb = StringBuilder(size * 2)
        for (byte in this) {
            sb.append(String.format("%02x", byte.toInt() and 0xFF))
        }
        return sb.toString()
    }
}