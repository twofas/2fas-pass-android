/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.Vault
import com.twofasapp.data.main.local.model.VaultEntity

internal class VaultMapper {

    fun mapToDomain(entity: VaultEntity): Vault {
        return with(entity) {
            Vault(
                id = id,
                name = name,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
        }
    }

    fun mapToEntity(domain: Vault): VaultEntity {
        return with(domain) {
            VaultEntity(
                id = id,
                createdAt = createdAt,
                updatedAt = updatedAt,
                name = name,
            )
        }
    }
}