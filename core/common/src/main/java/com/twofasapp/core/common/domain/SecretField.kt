/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.domain

import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.ktx.encodeBase64

sealed interface SecretField {
    data class Encrypted(val value: EncryptedBytes) : SecretField
    data class ClearText(val value: String) : SecretField
}

val SecretField?.isClearText: Boolean
    get() = this is SecretField.ClearText

val SecretField?.clearTextOrNull: String?
    get() = (this as? SecretField.ClearText)?.value

val SecretField?.clearText: String
    get() = (this as SecretField.ClearText).value

val SecretField?.encryptedText: String
    get() = (this as SecretField.Encrypted).value.bytes.encodeBase64()