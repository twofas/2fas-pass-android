/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.services

import com.twofasapp.data.cloud.domain.CloudConfig

interface CloudServiceProvider {
    fun provide(cloudConfig: CloudConfig): CloudService
}