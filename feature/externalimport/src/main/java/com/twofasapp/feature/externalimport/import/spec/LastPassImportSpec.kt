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
import com.twofasapp.core.common.domain.ImportType
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.common.ktx.readTextFile
import com.twofasapp.core.locale.R
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.feature.externalimport.import.CsvFile
import com.twofasapp.feature.externalimport.import.ImportContent
import com.twofasapp.feature.externalimport.import.ImportSpec

internal class LastPassImportSpec(
    private val vaultsRepository: VaultsRepository,
    private val context: Context,
) : ImportSpec {
    override val type = ImportType.LastPass
    override val name = "LastPass"
    override val image = com.twofasapp.core.design.R.drawable.external_logo_lastpass
    override val instructions = context.getString(com.twofasapp.core.locale.R.string.transfer_instructions_lastpass)
    override val cta: List<ImportSpec.Cta> = listOf(
        ImportSpec.Cta.Primary(
            text = context.getString(R.string.transfer_instructions_cta_csv),
            action = ImportSpec.CtaAction.ChooseFile,
        ),
    )

    override suspend fun readContent(uri: Uri): ImportContent {
        val vaultId = vaultsRepository.getVault().id

        val csvFile = CsvFile(
            text = context.readTextFile(uri),
            delimiter = ',',
            schemas = listOf(
                CsvFile.Schema.Login(
                    name = listOf("name"),
                    url = listOf("url"),
                    username = listOf("username"),
                    password = listOf("password"),
                    notes = listOf("extra"),
                ),
            ),
        )

        return ImportContent(
            items = csvFile.parse(vaultId).map { item ->
                val loginContent = item.content as ItemContent.Login
                val url = loginContent.uris.firstOrNull()?.text.orEmpty()
                val notes = loginContent.notes.orEmpty()

                // Check if this is a secure note (url starts with "http://sn" and extra/notes doesn't start with "NoteType:")
//                if (url.startsWith("http://sn", ignoreCase = true) && !notes.startsWith("NoteType:", ignoreCase = true)) {
                if (url.startsWith("http://sn", ignoreCase = true)) {
                    convertToSecureNote(item, vaultId)
                } else {
                    val filteredUris = loginContent.uris.filterNot { it.text.equals("http://sn", true) }

                    item.copy(
                        content = loginContent.copy(
                            uris = filteredUris,
                            iconUriIndex = if (filteredUris.isEmpty()) null else 0,
                        ),
                    )
                }
            },
            skipped = 0,
        )
    }

    private fun convertToSecureNote(item: Item, vaultId: String): Item {
        val loginContent = item.content as ItemContent.Login

        return Item.create(
            vaultId = vaultId,
            contentType = ItemContentType.SecureNote,
            content = ItemContent.SecureNote(
                name = loginContent.name,
                text = loginContent.notes?.let { SecretField.ClearText(it) },
            ),
        )
    }
}