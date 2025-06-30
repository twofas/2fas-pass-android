/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.security.crypto

import com.twofasapp.core.common.domain.crypto.KdfSpec

interface KdfCalculator {
    fun kdf(
        input: ByteArray,
        salt: ByteArray,
        kdfSpec: KdfSpec,
    ): ByteArray
}