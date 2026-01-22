/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.externalimport.import

import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContentType

internal data class ImportContent(
    val items: List<Item>,
    val tags: List<Tag>,
    val unknownItems: Int,
) {
    val countLogins: Int
        get() = items.count { it.contentType is ItemContentType.Login }

    val countSecureNotes: Int
        get() = items.count { it.contentType is ItemContentType.SecureNote } - unknownItems

    val countPaymentCards: Int
        get() = items.count { it.contentType is ItemContentType.PaymentCard }
}