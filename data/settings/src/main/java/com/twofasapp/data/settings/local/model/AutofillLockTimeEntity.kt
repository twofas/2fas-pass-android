/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.settings.local.model

internal enum class AutofillLockTimeEntity {
    Minutes5,
    Minutes15,
    Minutes30,
    Hour1,
    Day1,
    Never,
}