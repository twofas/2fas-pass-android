/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.services.s3

import io.kotest.matchers.shouldBe
import org.junit.Test

class S3EndpointDetectorTest {

    @Test
    fun `should detect legacy global endpoint as us-east-1`() {
        S3EndpointDetector.detect("https://s3.amazonaws.com") shouldBe
            S3EndpointDetection(region = "us-east-1", bucket = null)
    }

    @Test
    fun `should detect region from regional endpoint`() {
        S3EndpointDetector.detect("https://s3.eu-west-1.amazonaws.com") shouldBe
            S3EndpointDetection(region = "eu-west-1", bucket = null)
    }

    @Test
    fun `should detect region from legacy hyphen endpoint`() {
        S3EndpointDetector.detect("https://s3-eu-west-1.amazonaws.com") shouldBe
            S3EndpointDetection(region = "eu-west-1", bucket = null)
    }

    @Test
    fun `should detect bucket and region from virtual-hosted endpoint`() {
        S3EndpointDetector.detect("https://my-bucket.s3.eu-central-1.amazonaws.com") shouldBe
            S3EndpointDetection(region = "eu-central-1", bucket = "my-bucket")
    }

    @Test
    fun `should detect bucket from virtual-hosted endpoint without region`() {
        S3EndpointDetector.detect("https://my-bucket.s3.amazonaws.com") shouldBe
            S3EndpointDetection(region = "us-east-1", bucket = "my-bucket")
    }

    @Test
    fun `should detect bucket and region from legacy hyphen virtual-hosted endpoint`() {
        S3EndpointDetector.detect("https://my-bucket.s3-us-west-2.amazonaws.com") shouldBe
            S3EndpointDetection(region = "us-west-2", bucket = "my-bucket")
    }

    @Test
    fun `should detect bucket from path-style endpoint`() {
        S3EndpointDetector.detect("https://s3.eu-west-1.amazonaws.com/my-bucket") shouldBe
            S3EndpointDetection(region = "eu-west-1", bucket = "my-bucket")
    }

    @Test
    fun `should add scheme when missing`() {
        S3EndpointDetector.detect("s3.eu-west-1.amazonaws.com") shouldBe
            S3EndpointDetection(region = "eu-west-1", bucket = null)
    }

    @Test
    fun `should return null for non-aws endpoint`() {
        S3EndpointDetector.detect("https://minio.example.com") shouldBe null
    }

    @Test
    fun `should return null for blank endpoint`() {
        S3EndpointDetector.detect("") shouldBe null
    }
}