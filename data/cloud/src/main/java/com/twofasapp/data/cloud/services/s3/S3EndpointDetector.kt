/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.services.s3

import java.net.URI

data class S3EndpointDetection(
    val region: String?,
    val bucket: String?,
)

object S3EndpointDetector {

    fun detect(endpoint: String): S3EndpointDetection? {
        val uri = runCatching { URI(endpoint.trim().normalizeUrl()) }.getOrNull() ?: return null
        val host = uri.host?.lowercase() ?: return null

        val labels = host.split(".")
        if (labels.size < 2 || labels.takeLast(2).joinToString(".") != "amazonaws.com") return null

        val core = labels.dropLast(2)
        var region: String? = null
        var bucket: String? = null

        when {
            // s3.amazonaws.com — legacy global, defaults to us-east-1
            core.size == 1 && core[0] == "s3" -> {
                region = "us-east-1"
            }

            // s3.<region>.amazonaws.com
            core.size >= 2 && core[0] == "s3" -> {
                region = core[1]
            }

            // s3-<region>.amazonaws.com (legacy hyphen)
            core.size == 1 && core[0].startsWith("s3-") -> {
                region = core[0].removePrefix("s3-")
            }

            // <bucket>.s3[.<region>].amazonaws.com
            core.size >= 2 && core[1] == "s3" -> {
                bucket = core[0]
                region = if (core.size >= 3) core[2] else "us-east-1"
            }

            // <bucket>.s3-<region>.amazonaws.com (legacy hyphen + bucket)
            core.size == 2 && core[1].startsWith("s3-") -> {
                bucket = core[0]
                region = core[1].removePrefix("s3-")
            }
        }

        // Path-style endpoints carry the bucket as the first path segment.
        if (bucket == null) {
            bucket = uri.path?.split("/")?.firstOrNull { it.isNotEmpty() }
        }

        return S3EndpointDetection(region = region, bucket = bucket)
    }

    private fun String.normalizeUrl(): String =
        if (startsWith("http://", true).not() && startsWith("https://", true).not()) {
            "https://$this"
        } else {
            this
        }
}