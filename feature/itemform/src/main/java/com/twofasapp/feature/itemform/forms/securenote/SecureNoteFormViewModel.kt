/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform.forms.securenote

import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.data.main.TagsRepository
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.settings.SettingsRepository
import com.twofasapp.feature.itemform.ItemFormViewModel

internal class SecureNoteFormViewModel(
    vaultsRepository: VaultsRepository,
    settingsRepository: SettingsRepository,
    tagsRepository: TagsRepository,
) : ItemFormViewModel<ItemContent.SecureNote>(
    vaultsRepository = vaultsRepository,
    settingsRepository = settingsRepository,
    tagsRepository = tagsRepository,
) {
    fun updateName(text: String) {
        updateItemContent { content -> content.copy(name = text) }
    }

    fun updateText(text: String) {
        updateItemContent { content -> content.copy(text = SecretField.ClearText(text)) }
    }
}