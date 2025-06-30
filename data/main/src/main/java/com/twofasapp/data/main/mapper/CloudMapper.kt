/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.main.domain.CloudSyncInfo
import com.twofasapp.data.main.local.model.CloudConfigEntity
import com.twofasapp.data.main.local.model.CloudSyncInfoEntity

internal class CloudMapper {
    fun mapToEntity(domain: CloudSyncInfo): CloudSyncInfoEntity {
        return with(domain) {
            CloudSyncInfoEntity(
                enabled = enabled,
                config = config?.let { mapToEntity(it) },
                lastSuccessfulSyncTime = lastSuccessfulSyncTime,
            )
        }
    }

    fun mapToDomain(entity: CloudSyncInfoEntity): CloudSyncInfo {
        return with(entity) {
            CloudSyncInfo(
                enabled = enabled,
                config = config?.let { mapToDomain(it) },
                lastSuccessfulSyncTime = lastSuccessfulSyncTime,
            )
        }
    }

    fun mapToEntity(domain: CloudConfig): CloudConfigEntity {
        return when (domain) {
            is CloudConfig.GoogleDrive -> with(domain) {
                CloudConfigEntity.GoogleDrive(
                    id = id,
                    credentialType = credentialType,
                )
            }

            is CloudConfig.WebDav -> with(domain) {
                CloudConfigEntity.WebDav(
                    username = username,
                    password = password,
                    url = url,
                    allowUntrustedCertificate = allowUntrustedCertificate,
                )
            }
        }
    }

    fun mapToDomain(entity: CloudConfigEntity): CloudConfig {
        return when (entity) {
            is CloudConfigEntity.GoogleDrive -> with(entity) {
                CloudConfig.GoogleDrive(
                    id = id,
                    credentialType = credentialType,
                )
            }

            is CloudConfigEntity.WebDav -> with(entity) {
                CloudConfig.WebDav(
                    url = url,
                    username = username,
                    password = password,
                    allowUntrustedCertificate = allowUntrustedCertificate,
                )
            }
        }
    }
}