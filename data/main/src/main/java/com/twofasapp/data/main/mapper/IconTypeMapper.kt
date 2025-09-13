/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.IconType

class IconTypeMapper {

    fun mapToEntity(domain: IconType): Int {
        return when (domain) {
            IconType.Icon -> 0
            IconType.Label -> 1
            IconType.CustomImageUrl -> 2
        }
    }

    fun mapToJson(domain: IconType): Int {
        return when (domain) {
            IconType.Icon -> 0
            IconType.Label -> 1
            IconType.CustomImageUrl -> 2
        }
    }

    fun mapToDomainFromEntity(entity: Int): IconType {
        return when (entity) {
            0 -> IconType.Icon
            1 -> IconType.Label
            2 -> IconType.CustomImageUrl
            else -> IconType.Icon
        }
    }

    fun mapToDomainFromJson(json: Int): IconType {
        return when (json) {
            0 -> IconType.Icon
            1 -> IconType.Label
            2 -> IconType.CustomImageUrl
            else -> IconType.Icon
        }
    }
}