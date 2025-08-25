package com.twofasapp.feature.settings.ui.tags

import com.twofasapp.core.common.domain.Tag

internal data class ManageTagsUiState(
    val vaultId: String = "",
    val tags: List<Tag> = emptyList(),
)