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
import com.twofasapp.data.main.remote.model.ItemContentJson
import com.twofasapp.data.main.remote.model.vaultbackup.LoginUriJson

class ItemUriMapper(
    private val uriMatcherMapper: UriMatcherMapper,
) {
    internal fun mapToDomain(entity: LoginContentEntityV1.UriJson): ItemUri {
        return with(entity) {
            ItemUri(
                text = text,
                matcher = matcher.let { uriMatcherMapper.mapToDomainFromJson(it) },
            )
        }
    }

    internal fun mapToEntity(domain: ItemUri): LoginContentEntityV1.UriJson {
        return with(domain) {
            LoginContentEntityV1.UriJson(
                text = text,
                matcher = matcher.let { uriMatcherMapper.mapToEntity(it) },
            )
        }
    }

    internal fun mapToJson(domain: ItemUri): LoginUriJson {
        return with(domain) {
            LoginUriJson(
                text = text,
                matcher = uriMatcherMapper.mapToJson(matcher),
            )
        }
    }

    internal fun mapToDomain(json: LoginUriJson): ItemUri {
        return with(json) {
            ItemUri(
                text = text,
                matcher = matcher.let { uriMatcherMapper.mapToDomainFromJson(it) },
            )
        }
    }

    internal fun mapToItemContentJson(domain: ItemUri): ItemContentJson.Login.UriJson {
        return with(domain) {
            ItemContentJson.Login.UriJson(
                text = text,
                matcher = uriMatcherMapper.mapToJson(matcher),
            )
        }
    }

    internal fun mapToDomain(json: ItemContentJson.Login.UriJson): ItemUri {
        return with(json) {
            ItemUri(
                text = text,
                matcher = matcher.let { uriMatcherMapper.mapToDomainFromJson(it) },
            )
        }
    }
}