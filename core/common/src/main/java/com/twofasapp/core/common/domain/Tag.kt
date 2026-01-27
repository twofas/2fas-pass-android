/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */


package com.twofasapp.core.common.domain

data class Tag(
    val id: String,
    val vaultId: String,
    val name: String,
    val color: TagColor?,
    val position: Int,
    val updatedAt: Long,
    val assignedItemsCount: Int,
) {
    companion object {
        val Empty = Tag(
            id = "",
            vaultId = "",
            name = "",
            color = null,
            position = 0,
            updatedAt = 0,
            assignedItemsCount = 0,
        )

        fun create(
            vaultId: String,
            id: String,
            name: String? = null,
        ): Tag {
            return Tag(
                vaultId = vaultId,
                id = id,
                name = name.orEmpty(),
                color = null,
                position = 0,
                updatedAt = 0,
                assignedItemsCount = 0,
            )
        }
    }
}