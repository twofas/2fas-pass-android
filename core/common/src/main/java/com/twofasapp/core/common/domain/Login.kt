/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.domain

import java.time.Instant

data class Login(
    override val id: String = "",
    override val vaultId: String,
    override val createdAt: Long = 0,
    override val updatedAt: Long = 0,
    override val deletedAt: Long? = null,
    override val deleted: Boolean = false,
    override val securityType: SecurityType,
    override val tagIds: List<String>,
    val name: String,
    val username: String?,
    val password: SecretField?,
    val uris: List<LoginUri>,
    val iconType: IconType,
    val iconUriIndex: Int? = null,
    val customImageUrl: String? = null,
    val labelText: String? = null,
    val labelColor: String? = null,
    val notes: String? = null,
) : Item {

    val iconUrl: String?
        get() = iconUriIndex?.let { uris.getOrNull(it)?.iconUrl }

    val defaultLabelText: String
        get() = name.trim().take(2).uppercase()

    fun isContentEqual(login: Login): Boolean {
        return name == login.name &&
            username == login.username &&
            password == login.password &&
            uris == login.uris
    }

    companion object {
        val Empty: Login
            get() = Login(
                id = "",
                vaultId = "",
                name = "",
                username = null,
                password = SecretField.Visible(""),
                securityType = SecurityType.Tier3,
                uris = listOf(LoginUri("")),
                iconType = IconType.Icon,
                iconUriIndex = 0,
                customImageUrl = null,
                labelText = null,
                labelColor = null,
                tagIds = emptyList(),
                deleted = false,
                createdAt = 0L,
                updatedAt = 0L,
            )

        val Preview: Login
            get() = Login(
                id = "uuid",
                vaultId = "",
                name = "Name",
                username = "user@mail.com",
                password = SecretField.Visible(""),
                securityType = SecurityType.Tier2,
                uris = listOf(
                    LoginUri("https://2fas.com", LoginUriMatcher.Domain),
                    LoginUri("https://google.com", LoginUriMatcher.Host),
                ),
                iconType = IconType.Label,
                customImageUrl = null,
                labelText = "NA",
                labelColor = "#FF55FF",
                tagIds = emptyList(),
                deleted = false,
                createdAt = Instant.now().toEpochMilli(),
                updatedAt = Instant.now().toEpochMilli(),
            )
    }
}