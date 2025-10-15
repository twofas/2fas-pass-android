package com.twofasapp.core.common.domain.items

import com.twofasapp.core.common.domain.SecurityType

interface ItemSpec {
    val id: String
    val vaultId: String
    val createdAt: Long
    val updatedAt: Long
    val deletedAt: Long?
    val deleted: Boolean
    val securityType: SecurityType
    val contentType: ItemContentType
    val tagIds: List<String>
}