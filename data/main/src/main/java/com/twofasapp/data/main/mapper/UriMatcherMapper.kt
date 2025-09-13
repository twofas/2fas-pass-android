/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.UriMatcher

class UriMatcherMapper {

    fun mapToEntity(domain: UriMatcher): Int {
        return when (domain) {
            UriMatcher.Domain -> 0
            UriMatcher.Host -> 1
            UriMatcher.StartsWith -> 2
            UriMatcher.Exact -> 3
        }
    }

    fun mapToJson(domain: UriMatcher): Int {
        return when (domain) {
            UriMatcher.Domain -> 0
            UriMatcher.Host -> 1
            UriMatcher.StartsWith -> 2
            UriMatcher.Exact -> 3
        }
    }

    fun mapToDomainFromEntity(entity: Int?): UriMatcher {
        return when (entity) {
            0 -> UriMatcher.Domain
            1 -> UriMatcher.Host
            2 -> UriMatcher.StartsWith
            3 -> UriMatcher.Exact
            else -> UriMatcher.Domain
        }
    }

    fun mapToDomainFromJson(json: Int?): UriMatcher {
        return when (json) {
            0 -> UriMatcher.Domain
            1 -> UriMatcher.Host
            2 -> UriMatcher.StartsWith
            3 -> UriMatcher.Exact
            else -> UriMatcher.Domain
        }
    }
}