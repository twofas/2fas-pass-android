package com.twofasapp.core.common.domain

data class Tag(
    val id: String,
    val vaultId: String,
    val name: String,
    val color: String?,
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
    }
}