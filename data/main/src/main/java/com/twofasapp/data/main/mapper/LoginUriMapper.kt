/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.LoginUri
import com.twofasapp.data.main.local.model.items.LoginContentEntityV1
import com.twofasapp.data.main.remote.model.LoginUriJson

class LoginUriMapper(
    private val uriMatcherMapper: LoginUriMatcherMapper,
) {
    fun mapToDomain(entity: LoginContentEntityV1.UriJson): LoginUri {
        return with(entity) {
            LoginUri(
                text = text,
                matcher = matcher.let { uriMatcherMapper.mapToDomainFromJson(it) },
            )
        }
    }

    fun mapToEntity(domain: LoginUri): LoginContentEntityV1.UriJson {
        return with(domain) {
            LoginContentEntityV1.UriJson(
                text = text,
                matcher = matcher.let { uriMatcherMapper.mapToEntity(it) },
            )
        }
    }

    fun mapToJson(domain: LoginUri): LoginUriJson {
        return with(domain) {
            LoginUriJson(
                text = text,
                matcher = uriMatcherMapper.mapToJson(matcher),
            )
        }
    }

    fun mapToDomain(json: LoginUriJson): LoginUri {
        return with(json) {
            LoginUri(
                text = text,
                matcher = matcher.let { uriMatcherMapper.mapToDomainFromJson(it) },
            )
        }
    }
}