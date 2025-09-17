/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.exceptions

fun CloudError?.asMessage(): String {
    return when (this) {
        is CloudError.Unknown -> "Unknown error, please try again.${this.cause?.let { " (${it.message})" }}"
        is CloudError.AuthenticationError -> "Cloud authentication error, please try again."
        is CloudError.NoNetwork -> "No connection, please turn your Internet and try again."
        is CloudError.GetFile -> "Error when fetching backup file from cloud."
        is CloudError.CreateFile -> "Error when creating backup file on cloud."
        is CloudError.UpdateFile -> "Error when updating backup file on cloud."
        is CloudError.FileParsing -> "Error when parsing backup file from cloud."
        is CloudError.LocalAccountDoesNotExist -> "Google Drive account details does not exists. Please try to restart sync."
        is CloudError.NotAuthorized -> "Google Drive is not authorized. Please try to login again and restart sync."
        is CloudError.WrongBackupPassword -> "Your backup on Google Drive is encrypted with a different Master Password. You can either change your Master Password to match the one used for Google Drive, or replace the backup with a new password. Note that replacing the backup with a new password will disconnect other devices from the sync."
        is CloudError.FileIsLocked -> "Backup file is locked. Please try to restart sync."
        is CloudError.MultiDeviceSyncNotAvailable -> "Matching vault exists in the backup but is linked to another device. Upgrade your plan to enable multi-device sync."
        is CloudError.CleartextNotPermitted -> "HTTP traffic is not permitted for security reasons. Please use HTTPS instead."
        is CloudError.InvalidSchemaVersion -> "Cloud sync failed. The Vault you’re trying to synchronize was created in a newer version $backupSchemaVersion, which is not supported in your current version. Please update your app to synchronize it."
        null -> "Unknown error, please try again."
    }
}

fun CloudError?.asCode(): Int {
    return when (this) {
        is CloudError.Unknown -> 0
        is CloudError.AuthenticationError -> 1
        is CloudError.NoNetwork -> 2
        is CloudError.GetFile -> 3
        is CloudError.CreateFile -> 4
        is CloudError.UpdateFile -> 5
        is CloudError.FileParsing -> 6
        is CloudError.LocalAccountDoesNotExist -> 7
        is CloudError.NotAuthorized -> 8
        is CloudError.WrongBackupPassword -> 9
        is CloudError.FileIsLocked -> 10
        is CloudError.MultiDeviceSyncNotAvailable -> 11
        is CloudError.CleartextNotPermitted -> 12
        is CloudError.InvalidSchemaVersion -> 13
        null -> -1
    }
}