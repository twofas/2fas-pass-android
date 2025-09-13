package com.twofasapp.core.common.domain.items

import com.twofasapp.core.common.domain.SecurityType

data class Item(
    override val id: String = "",
    override val vaultId: String,
    override val createdAt: Long = 0,
    override val updatedAt: Long = 0,
    override val deletedAt: Long? = null,
    override val deleted: Boolean = false,
    override val securityType: SecurityType,
    override val contentType: String,
    override val contentVersion: Int,
    override val tagIds: List<String>,
    val content: ItemContent,
) : ItemSpec {
    companion object {
        val Empty = Item(
            id = "",
            vaultId = "",
            createdAt = 0,
            updatedAt = 0,
            deletedAt = 0,
            deleted = false,
            securityType = SecurityType.Tier3,
            contentType = "",
            contentVersion = 1,
            tagIds = emptyList(),
            content = ItemContent.Unknown(""),
        )

        fun create(
            contentType: String,
            content: ItemContent,
            vaultId: String = "",
            securityType: SecurityType = SecurityType.Tier3,
        ): Item {
            return Empty.copy(
                contentType = contentType,
                content = content,
                vaultId = vaultId,
                securityType = securityType,
            )
        }
    }
}