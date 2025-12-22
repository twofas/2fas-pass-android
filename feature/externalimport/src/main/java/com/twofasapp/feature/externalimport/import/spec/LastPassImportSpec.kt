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
import com.twofasapp.core.common.crypto.Uuid
import com.twofasapp.core.common.domain.ImportType
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.common.ktx.readTextFile
import com.twofasapp.core.locale.R
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.feature.externalimport.import.CsvParser
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
        var unknownItems = 0
        val tags = mutableListOf<Tag>()

        val items = buildList {
            CsvParser.parse(
                text = context.readTextFile(uri),
            ) { row ->

                val tagIds: List<String> = row.get("grouping")
                    .orEmpty()
                    .split('\\')
                    .asSequence()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .map { tagName ->
                        val existingTag = tags.firstOrNull { it.name == tagName }

                        val tag = existingTag ?: Tag.create(
                            vaultId = vaultId,
                            id = Uuid.generate(),
                            name = tagName,
                        ).also {
                            tags.add(it)
                        }

                        tag.id
                    }
                    .toList()

                if (row.get("url").orEmpty().startsWith("http://sn", ignoreCase = true)) {
                    val extras = row.get("extra").orEmpty()

                    when {
                        // TODO: Handle "NoteType:Credit Card" when Payment Cards implemented
                        extras.startsWith("NoteType:") -> {
                            // Add unknown types as note
                            add(
                                Item.create(
                                    vaultId = vaultId,
                                    tagIds = tagIds,
                                    contentType = ItemContentType.SecureNote,
                                    content = ItemContent.SecureNote.create(
                                        name = row.get("name"),
                                        text = row.get("extra"),
                                    ),
                                ),
                            )

                            unknownItems++
                        }

                        else -> {
                            // Add secure note
                            add(
                                Item.create(
                                    vaultId = vaultId,
                                    tagIds = tagIds,
                                    contentType = ItemContentType.SecureNote,
                                    content = ItemContent.SecureNote.create(
                                        name = row.get("name"),
                                        text = row.get("extra"),
                                    ),
                                ),
                            )
                        }
                    }
                } else {
                    // Add login
                    add(
                        Item.create(
                            vaultId = vaultId,
                            tagIds = tagIds,
                            contentType = ItemContentType.Login,
                            content = ItemContent.Login.create(
                                name = row.get("name"),
                                username = row.get("username"),
                                password = row.get("password"),
                                url = row.get("url"),
                                notes = row.get("extra"),
                            ),
                        ),
                    )
                }
            }
        }

        return ImportContent(
            items = items,
            tags = tags,
            unknownItems = unknownItems,
        )
    }
}