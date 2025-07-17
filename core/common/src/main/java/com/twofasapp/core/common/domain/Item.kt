package com.twofasapp.core.common.domain

interface Item {
    val id: String
    val vaultId: String
    val createdAt: Long
    val updatedAt: Long
    val deletedAt: Long?
    val deleted: Boolean
    val securityType: SecurityType
    val tagIds: List<String>
}