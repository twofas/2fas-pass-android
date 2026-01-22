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
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.common.ktx.readTextFile
import com.twofasapp.core.locale.R
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.feature.externalimport.import.CsvParser
import com.twofasapp.feature.externalimport.import.ImportContent
import com.twofasapp.feature.externalimport.import.ImportSpec

internal class ChromeImportSpec(
    private val vaultsRepository: VaultsRepository,
    private val context: Context,
) : ImportSpec() {
    override val type = ImportType.Chrome
    override val name = "Chrome"
    override val image = com.twofasapp.core.design.R.drawable.external_logo_chrome
    override val instructions = context.getString(R.string.transfer_instructions_chrome)
    override val additionalInfo = null
    override val cta: List<Cta> = listOf(
        Cta.Primary(
            text = context.getString(R.string.transfer_instructions_cta_csv),
            action = CtaAction.ChooseFile,
        ),
    )

    override suspend fun readContent(uri: Uri): ImportContent {
        val vaultId = vaultsRepository.getVault().id

        val items = buildList {
            CsvParser.parse(
                text = context.readTextFile(uri),
            ) { row ->
                add(
                    Item.create(
                        vaultId = vaultId,
                        contentType = ItemContentType.Login,
                        content = ItemContent.Login.create(
                            name = row.get("name"),
                            username = row.get("username"),
                            password = row.get("password"),
                            url = row.get("url"),
                            notes = row.get("note"),
                        ),
                    ),
                )
            }
        }

        return ImportContent(
            items = items,
            tags = emptyList(),
            unknownItems = 0,
        )
    }
}