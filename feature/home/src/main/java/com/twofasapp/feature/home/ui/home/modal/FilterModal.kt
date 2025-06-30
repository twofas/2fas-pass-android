/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.home.modal

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.foundation.checked.CheckIcon
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.settings.domain.SortingMethod

@Composable
internal fun FilterModal(
    onDismissRequest: () -> Unit,
    selected: SortingMethod,
    onSelect: (SortingMethod) -> Unit = {},
) {
    Modal(
        onDismissRequest = onDismissRequest,
        headerText = MdtLocale.strings.loginFilterModalTitle,
    ) { dismissAction ->
        Content(
            selected = selected,
            onSelect = { dismissAction { onSelect(it) } },
        )
    }
}

@Composable
private fun Content(
    selected: SortingMethod,
    onSelect: (SortingMethod) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        SortingMethod.entries.forEach { method ->
            OptionEntry(
                title = method.asTitle(),
                onClick = { onSelect(method) },
                content = { CheckIcon(checked = method == selected) },
            )
        }
    }
}

@Composable
internal fun SortingMethod.asTitle(): String {
    val strings = MdtLocale.strings

    return when (this) {
        SortingMethod.NameAsc -> strings.loginFilterModalSortNameAsc
        SortingMethod.NameDesc -> strings.loginFilterModalSortNameDesc
        SortingMethod.CreationDateAsc -> strings.loginFilterModalSortCreationDateAsc
        SortingMethod.CreationDateDesc -> strings.loginFilterModalSortCreationDateDesc
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            selected = SortingMethod.NameAsc,
        )
    }
}