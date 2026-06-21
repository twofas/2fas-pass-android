/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.restorevault

import com.twofasapp.data.cloud.domain.CloudConnection

internal class RestoreState {
    var restoreSource: RestoreSource = RestoreSource.GoogleDrive
    var restoreFile: RestoreFile? = null
    var cloudConnection: CloudConnection? = null

    fun reset() {
        restoreSource = RestoreSource.GoogleDrive
        cloudConnection = null
        restoreFile = null
    }
}