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

internal class DashlaneDesktopImportSpec(
    private val vaultsRepository: VaultsRepository,
    private val context: Context,
) : ImportSpec {
    override val type = ImportType.DashlaneDesktop
    override val name = "Dashlane (Desktop)"
    override val image = com.twofasapp.core.design.R.drawable.external_logo_dashlane
    override val instructions = context.getString(com.twofasapp.core.locale.R.string.transfer_instructions_dashlane_pc)
    override val cta: List<ImportSpec.Cta> = listOf(
        ImportSpec.Cta.Primary(
            text = context.getString(R.string.transfer_instructions_cta_zip),
            action = ImportSpec.CtaAction.ChooseFile,
        ),
    )

    override suspend fun readContent(uri: Uri): ImportContent {
        val vaultId = vaultsRepository.getVault().id
        val items = mutableListOf<Item>()
        val credentialsFilename = "credentials.csv"
        val notesFilename = "securenotes.csv"

        val zipFile = ZipFile(
            uri = uri,
        )

        val zipFileContents = zipFile.read(
            context = context,
            filter = { filename ->
                filename.equals(credentialsFilename, true) ||
                    filename.equals(notesFilename, true)
            },
        )

        zipFileContents[credentialsFilename]?.let { text ->
            val csvFile = CsvFile(
                text = text,
                delimiter = ',',
                schemas = listOf(
                    CsvFile.Schema.Login(
                        name = listOf("title"),
                        url = listOf("url"),
                        username = listOf("username", "username2", "username3"),
                        password = listOf("password"),
                        notes = listOf("note"),
                    ),
                ),
            )

            items.addAll(csvFile.parse(vaultId))
        }

        zipFileContents[notesFilename]?.let { text ->
            val csvFile = CsvFile(
                text = text,
                delimiter = ',',
                schemas = listOf(
                    CsvFile.Schema.SecureNote(
                        name = listOf("title"),
                        text = listOf("note"),
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