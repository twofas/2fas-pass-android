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
import com.twofasapp.core.locale.R
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.feature.externalimport.import.CsvFile
import com.twofasapp.feature.externalimport.import.ImportContent
import com.twofasapp.feature.externalimport.import.ImportSpec
import com.twofasapp.feature.externalimport.import.ZipFile

internal class AppleMobileImportSpec(
    private val vaultsRepository: VaultsRepository,
    private val context: Context,
) : ImportSpec() {
    override val type = ImportType.AppleDesktop
    override val name = "Apple Passwords (Mobile)"
    override val image = com.twofasapp.core.design.R.drawable.external_logo_apple
    override val instructions = context.getString(R.string.transfer_instructions_apple_passwords_mobile)
    override val cta: List<ImportSpec.Cta> = listOf(
        ImportSpec.Cta.Primary(
            text = context.getString(R.string.transfer_instructions_cta_zip),
            action = ImportSpec.CtaAction.ChooseFile,
        ),
    )

    override suspend fun readContent(uri: Uri): ImportContent {
        val vaultId = vaultsRepository.getVault().id
        val items = mutableListOf<Item>()

        val zipFile = ZipFile(
            uri = uri,
        )

        val zipFileContents = zipFile.read(
            context = context,
            filter = { filename -> filename.endsWith(".csv", true) },
        )

        zipFileContents.values.firstOrNull()?.let {
            val csvFile = CsvFile(
                text = it,
                delimiter = ',',
                schemas = listOf(
                    CsvFile.Schema.Login(
                        name = listOf("Title"),
                        url = listOf("URL"),
                        username = listOf("Username"),
                        password = listOf("Password"),
                        notes = listOf("Notes"),
                    ),
                ),
            )

            items.addAll(csvFile.parse(vaultId))
        }

        return ImportContent(
            items = items,
            tags = emptyList(),
            unknownItems = 0,
        )
    }
}