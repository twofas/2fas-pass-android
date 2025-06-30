/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.restorevault

import android.net.Uri
import com.twofasapp.data.cloud.domain.CloudFileInfo

sealed interface RestoreFile {
    data class Cloud(
        val fileInfo: CloudFileInfo,
    ) : RestoreFile

    data class LocalFile(
        val uri: Uri,
    ) : RestoreFile
}