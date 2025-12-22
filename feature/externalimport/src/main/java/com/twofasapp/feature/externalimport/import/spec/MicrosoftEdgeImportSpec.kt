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

internal class MicrosoftEdgeImportSpec(
    private val vaultsRepository: VaultsRepository,
    private val context: Context,
) : ImportSpec {
    override val type = ImportType.MicrosoftEdge
    override val name = "Microsoft Edge"
    override val image = com.twofasapp.core.design.R.drawable.external_logo_microsoft_edge
    override val instructions = context.getString(R.string.transfer_instructions_microsoft_edge)
    override val cta: List<ImportSpec.Cta> = listOf(
        ImportSpec.Cta.Primary(
            text = context.getString(R.string.transfer_instructions_cta_csv),
            action = ImportSpec.CtaAction.ChooseFile,
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
                            name = row["name"],
                            username = row["username"],
                            password = row["password"],
                            url = row["url"],
                            notes = row["note"],
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