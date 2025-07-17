/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.exceptions

import android.content.Intent

sealed class CloudError(val cause: Throwable?) {
    class Unknown(cause: Throwable? = null) : CloudError(cause)
    class AuthenticationError(cause: Throwable? = null) : CloudError(cause)
    class NoNetwork(cause: Throwable? = null) : CloudError(cause)
    class GetFile(cause: Throwable? = null) : CloudError(cause)
    class CreateFile(cause: Throwable? = null) : CloudError(cause)
    class UpdateFile(cause: Throwable? = null) : CloudError(cause)
    class FileParsing(cause: Throwable? = null) : CloudError(cause)
    class LocalAccountDoesNotExist(cause: Throwable? = null) : CloudError(cause)
    class NotAuthorized(cause: Throwable? = null, val intent: Intent?) : CloudError(cause)
    class WrongBackupPassword(cause: Throwable? = null) : CloudError(cause)
    class FileIsLocked(cause: Throwable? = null) : CloudError(cause)
    class MultiDeviceSyncNotAvailable(cause: Throwable? = null) : CloudError(cause)
    class CleartextNotPermitted(cause: Throwable? = null) : CloudError(cause)
}