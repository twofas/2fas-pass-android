package com.twofasapp.feature.settings.ui.tags

import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.TagColor

internal data class ManageTagsUiState(
    val vaultId: String = "",
    val tags: List<Tag> = emptyList(),
    val suggestedTagColor: TagColor = TagColor.default
)