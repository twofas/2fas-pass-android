/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.service.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class AutofillLogin(
    val encrypted: Boolean,
    val matchRank: Int?,
    val id: String,
    val name: String?,
    val username: String?,
    val password: String?,
    val uris: List<String>,
    val updatedAt: Long,
) : Parcelable {

    companion object {
        val Empty = AutofillLogin(
            encrypted = false,
            matchRank = null,
            id = "",
            name = null,
            username = null,
            password = null,
            uris = listOf(),
            updatedAt = 0L,
        )
    }
}