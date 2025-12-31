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
import com.twofasapp.core.common.ktx.readTextFile
import com.twofasapp.core.locale.R
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.feature.externalimport.import.CsvFile
import com.twofasapp.feature.externalimport.import.ImportContent
import com.twofasapp.feature.externalimport.import.ImportSpec

internal class AppleDesktopImportSpec(
    private val vaultsRepository: VaultsRepository,
    private val context: Context,
) : ImportSpec() {
    override val type = ImportType.AppleDesktop
    override val name = "Apple Passwords (Desktop)"
    override val image = com.twofasapp.core.design.R.drawable.external_logo_apple
    override val instructions = context.getString(R.string.transfer_instructions_apple_passwords_pc)
    override val cta: List<Cta> = listOf(
        Cta.Primary(
            text = context.getString(R.string.transfer_instructions_cta_csv),
            action = CtaAction.ChooseFile,
        ),
    )

    override suspend fun readContent(uri: Uri): ImportContent {
        val vaultId = vaultsRepository.getVault().id

        val csvFile = CsvFile(
            text = context.readTextFile(uri),
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

        return ImportContent(
            items = csvFile.parse(vaultId),
            tags = emptyList(),
            unknownItems = 0,
        )
    }
}