/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.LoginUriMatcher

internal class LoginUriMatcherMapper {

    fun mapToEntity(domain: LoginUriMatcher): Int {
        return when (domain) {
            LoginUriMatcher.Domain -> 0
            LoginUriMatcher.Host -> 1
            LoginUriMatcher.StartsWith -> 2
            LoginUriMatcher.Exact -> 3
        }
    }

    fun mapToJson(domain: LoginUriMatcher): Int {
        return when (domain) {
            LoginUriMatcher.Domain -> 0
            LoginUriMatcher.Host -> 1
            LoginUriMatcher.StartsWith -> 2
            LoginUriMatcher.Exact -> 3
        }
    }

    fun mapToDomainFromEntity(entity: Int?): LoginUriMatcher {
        return when (entity) {
            0 -> LoginUriMatcher.Domain
            1 -> LoginUriMatcher.Host
            2 -> LoginUriMatcher.StartsWith
            3 -> LoginUriMatcher.Exact
            else -> LoginUriMatcher.Domain
        }
    }

    fun mapToDomainFromJson(json: Int?): LoginUriMatcher {
        return when (json) {
            0 -> LoginUriMatcher.Domain
            1 -> LoginUriMatcher.Host
            2 -> LoginUriMatcher.StartsWith
            3 -> LoginUriMatcher.Exact
            else -> LoginUriMatcher.Domain
        }
    }
}