/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.domain

import com.twofasapp.data.cloud.exceptions.CloudError

sealed interface CloudResult {
    data object Success : CloudResult
    data class Failure(val error: CloudError) : CloudResult
}