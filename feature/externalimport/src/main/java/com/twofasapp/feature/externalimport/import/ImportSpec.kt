/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.externalimport.import

import android.net.Uri
import com.twofasapp.core.common.domain.ImportType
import com.twofasapp.core.design.foundation.preview.PreviewTextMedium

internal interface ImportSpec {
    val type: ImportType
    val name: String
    val image: Int
    val instructions: String
    val cta: List<Cta>

    suspend fun readContent(uri: Uri): ImportContent

    sealed interface Cta {
        data class Primary(
            val text: String,
            val action: CtaAction,
        ) : Cta
    }

    sealed interface CtaAction {
        data class ChooseFile(val type: String = "*/*") : CtaAction
    }

    companion object {
        val Empty = object : ImportSpec {
            override val type: ImportType = ImportType.Bitwarden
            override val name = "Name"
            override val image = com.twofasapp.core.design.R.drawable.ic_android
            override val instructions = "$PreviewTextMedium\n\n$PreviewTextMedium\n\n$PreviewTextMedium"
            override val cta = listOf<Cta>(Cta.Primary(text = "Choose file", action = CtaAction.ChooseFile()))
            override suspend fun readContent(uri: Uri): ImportContent = ImportContent(emptyList(), 0)
        }
    }
}