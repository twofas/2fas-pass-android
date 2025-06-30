/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.LoginSecurityType

internal class LoginSecurityTypeMapper {

    fun mapToEntity(domain: LoginSecurityType): Int {
        return when (domain) {
            LoginSecurityType.Tier1 -> 0
            LoginSecurityType.Tier2 -> 1
            LoginSecurityType.Tier3 -> 2
        }
    }

    fun mapToJson(domain: LoginSecurityType): Int {
        return when (domain) {
            LoginSecurityType.Tier1 -> 0
            LoginSecurityType.Tier2 -> 1
            LoginSecurityType.Tier3 -> 2
        }
    }

    fun mapToDomainFromEntity(entity: Int): LoginSecurityType {
        return when (entity) {
            0 -> LoginSecurityType.Tier1
            1 -> LoginSecurityType.Tier2
            2 -> LoginSecurityType.Tier3
            else -> LoginSecurityType.Tier3
        }
    }

    fun mapToDomainFromJson(json: Int): LoginSecurityType {
        return when (json) {
            0 -> LoginSecurityType.Tier1
            1 -> LoginSecurityType.Tier2
            2 -> LoginSecurityType.Tier3
            else -> LoginSecurityType.Tier3
        }
    }
}