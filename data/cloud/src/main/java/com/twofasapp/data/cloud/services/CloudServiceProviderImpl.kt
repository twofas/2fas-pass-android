/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.services

import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.cloud.services.googledrive.GoogleDriveCloudService
import com.twofasapp.data.cloud.services.s3.S3CloudService
import com.twofasapp.data.cloud.services.webdav.WebDavCloudService

internal class CloudServiceProviderImpl(
    private val googleDriveCloudService: GoogleDriveCloudService,
    private val webDavCloudService: WebDavCloudService,
    private val s3CloudService: S3CloudService,
) : CloudServiceProvider {

    override fun provide(cloudConfig: CloudConfig): CloudService {
        return when (cloudConfig) {
            is CloudConfig.GoogleDrive -> googleDriveCloudService
            is CloudConfig.WebDav -> webDavCloudService
            is CloudConfig.S3 -> s3CloudService
        }
    }
}