/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.domain

import com.twofasapp.core.common.domain.DeletedItem
import com.twofasapp.core.common.domain.Login

data class CloudMerge(
    val logins: Result<Login>,
    val tags: Result<Tag>,
    val deletedItems: MutableList<DeletedItem> = mutableListOf(),
) {
    data class Result<T>(
        val toAdd: MutableList<T> = mutableListOf(),
        val toUpdate: MutableList<T> = mutableListOf(),
        val toDelete: MutableList<T> = mutableListOf(),
    )
}