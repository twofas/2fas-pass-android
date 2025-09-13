/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.ItemUri
import com.twofasapp.data.main.local.model.items.LoginContentEntityV1
import com.twofasapp.data.main.remote.model.deprecated.LoginUriJson

class ItemUriMapper(
    private val uriMatcherMapper: UriMatcherMapper,
) {
    fun mapToDomain(entity: LoginContentEntityV1.UriJson): ItemUri {
        return with(entity) {
            ItemUri(
                text = text,
                matcher = matcher.let { uriMatcherMapper.mapToDomainFromJson(it) },
            )
        }
    }

    fun mapToEntity(domain: ItemUri): LoginContentEntityV1.UriJson {
        return with(domain) {
            LoginContentEntityV1.UriJson(
                text = text,
                matcher = matcher.let { uriMatcherMapper.mapToEntity(it) },
            )
        }
    }

    fun mapToJson(domain: ItemUri): LoginUriJson {
        return with(domain) {
            LoginUriJson(
                text = text,
                matcher = uriMatcherMapper.mapToJson(matcher),
            )
        }
    }

    fun mapToDomain(json: LoginUriJson): ItemUri {
        return with(json) {
            ItemUri(
                text = text,
                matcher = matcher.let { uriMatcherMapper.mapToDomainFromJson(it) },
            )
        }
    }
}