/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.SecurityType

internal class ItemSecurityTypeMapper {

    fun mapToEntity(domain: SecurityType): Int {
        return when (domain) {
            SecurityType.Tier1 -> 0
            SecurityType.Tier2 -> 1
            SecurityType.Tier3 -> 2
        }
    }

    fun mapToJson(domain: SecurityType): Int {
        return when (domain) {
            SecurityType.Tier1 -> 0
            SecurityType.Tier2 -> 1
            SecurityType.Tier3 -> 2
        }
    }

    fun mapToDomainFromEntity(entity: Int): SecurityType {
        return when (entity) {
            0 -> SecurityType.Tier1
            1 -> SecurityType.Tier2
            2 -> SecurityType.Tier3
            else -> SecurityType.Tier3
        }
    }

    fun mapToDomainFromJson(json: Int): SecurityType {
        return when (json) {
            0 -> SecurityType.Tier1
            1 -> SecurityType.Tier2
            2 -> SecurityType.Tier3
            else -> SecurityType.Tier3
        }
    }
}