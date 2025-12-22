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
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.ktx.readTextFile
import com.twofasapp.core.locale.R
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.feature.externalimport.import.CsvFile
import com.twofasapp.feature.externalimport.import.ImportContent
import com.twofasapp.feature.externalimport.import.ImportSpec

internal class FirefoxImportSpec(
    private val vaultsRepository: VaultsRepository,
    private val context: Context,
) : ImportSpec {
    override val type = ImportType.Firefox
    override val name = "Firefox"
    override val image = com.twofasapp.core.design.R.drawable.external_logo_firefox
    override val instructions = context.getString(R.string.transfer_instructions_firefox)
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
                    name = listOf("url"),
                    url = listOf("url"),
                    username = listOf("username"),
                    password = listOf("password"),
                    notes = emptyList(),
                ),
            ),
        )

        return ImportContent(
            items = csvFile.parse(vaultId)
                .filter { item ->
                    item.content.name.lowercase() != "chrome://FirefoxAccounts".lowercase()
                }
                .map { item ->
                    item.copy(
                        content = (item.content as ItemContent.Login).copy(
                            name = item.content.name.removePrefix("http://").removePrefix("https://"),
                        ),
                    )
                },
            tags = emptyList(),
            unknownItems = 0,
        )
    }
}