/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.externalimport.import

import android.net.Uri
import com.twofasapp.core.common.crypto.Uuid
import com.twofasapp.core.common.domain.ImportType
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.design.R
import com.twofasapp.core.design.foundation.preview.PreviewTextMedium

internal abstract class ImportSpec() {
    abstract val type: ImportType
    abstract val name: String
    abstract val image: Int
    abstract val instructions: String
    abstract val additionalInfo: String?
    abstract val cta: List<Cta>

    protected val tags: MutableList<Tag> = mutableListOf()

    abstract suspend fun readContent(uri: Uri): ImportContent

    sealed interface Cta {
        data class Primary(
            val text: String,
            val action: CtaAction,
        ) : Cta
    }

    sealed interface CtaAction {
        data object ChooseFile : CtaAction
    }

    companion object {
        val Empty = object : ImportSpec() {
            override val type: ImportType = ImportType.Bitwarden
            override val name = "Name"
            override val image = R.drawable.ic_android
            override val instructions = "$PreviewTextMedium\n\n$PreviewTextMedium\n\n$PreviewTextMedium"
            override val additionalInfo = PreviewTextMedium
            override val cta = listOf<Cta>(Cta.Primary(text = "Choose file", action = CtaAction.ChooseFile))
            override suspend fun readContent(uri: Uri): ImportContent = ImportContent(emptyList(), emptyList(), 0)
        }
    }

    protected fun resolveTagIds(
        raw: String?,
        vaultId: String,
        separator: Char,
    ): List<String> {
        val names = raw
            .orEmpty()
            .split(separator)
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        return names
            .map { name ->
                tags.firstOrNull { it.name == name }
                    ?: Tag.create(
                        vaultId = vaultId,
                        id = Uuid.generate(),
                        name = name,
                    ).also(tags::add)
            }
            .map(Tag::id)
            .toList()
    }
}