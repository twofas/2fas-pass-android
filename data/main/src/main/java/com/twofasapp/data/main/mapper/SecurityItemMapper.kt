/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.SecurityItem
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.data.main.local.model.ItemEntity

internal class SecurityItemMapper {

    fun mapToDomain(
        items: List<ItemEntity>
    ): List<SecurityItem> {
        return SecurityType.entries.map { securityType ->
            SecurityItem(
                type = securityType,
                assignedItemsCount = items.count { item -> item.securityType == securityType.ordinal })
        }
    }
}