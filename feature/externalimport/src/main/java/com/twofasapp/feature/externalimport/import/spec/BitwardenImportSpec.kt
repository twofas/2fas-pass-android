/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.externalimport.import.spec

import android.content.Context
import android.net.Uri
import com.twofasapp.core.common.domain.IconType
import com.twofasapp.core.common.domain.ImportType
import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.common.domain.LoginUri
import com.twofasapp.core.common.domain.LoginUriMatcher
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.ktx.readTextFile
import com.twofasapp.core.locale.R
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.feature.externalimport.import.ImportContent
import com.twofasapp.feature.externalimport.import.ImportSpec
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class BitwardenImportSpec(
    private val vaultsRepository: VaultsRepository,
    private val context: Context,
    private val json: Json,
) : ImportSpec {
    override val type = ImportType.Bitwarden
    override val name = "Bitwarden"
    override val image = com.twofasapp.core.design.R.drawable.external_logo_bitwarden
    override val instructions = context.getString(com.twofasapp.core.locale.R.string.transfer_instructions_bitwarden)
    override val cta: List<ImportSpec.Cta> = listOf(
        ImportSpec.Cta.Primary(
            text = context.getString(R.string.transfer_instructions_cta_json),
            action = ImportSpec.CtaAction.ChooseFile("application/json"),
        ),
    )

    override suspend fun readContent(uri: Uri): ImportContent {
        val content = json.decodeFromString<Model>(context.readTextFile(uri))
        val logins = content.items.orEmpty().map { item ->
            Login(
                name = item.name.orEmpty(),
                vaultId = vaultsRepository.getVault().id,
                username = item.login?.username,
                password = item.login?.password?.let { SecretField.Visible(it) },
                securityType = SecurityType.Tier3,
                uris = item.login?.uris.orEmpty().map { uri ->
                    LoginUri(
                        text = uri.uri.orEmpty(),
                        matcher = when (uri.match) {
                            0 -> LoginUriMatcher.Domain
                            1 -> LoginUriMatcher.Host
                            2 -> LoginUriMatcher.StartsWith
                            3 -> LoginUriMatcher.Exact
                            else -> LoginUriMatcher.Domain
                        },
                    )
                },
                iconType = IconType.Icon,
                iconUriIndex = if (item.login?.uris.isNullOrEmpty()) null else 0,
                notes = item.notes,
                tagIds = emptyList(),
            )
        }

        return ImportContent(
            logins = logins,
            skipped = 0,
        )
    }

    @Serializable
    private data class Model(
        val items: List<Item>?,
    )

    @Serializable
    private data class Item(
        val name: String?,
        val notes: String?,
        val login: LoginItem?,
    ) {
        @Serializable
        data class LoginItem(
            val username: String?,
            val password: String?,
            val uris: List<Uri>?,
        )

        @Serializable
        data class Uri(
            val uri: String?,
            val match: Int?,
        )
    }
}