package com.twofasapp.feature.home.ui.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.menu.DropdownMenu
import com.twofasapp.core.design.foundation.menu.DropdownMenuItem

@Composable
internal fun HomeListDropdownMenu(
    selectedTag: Tag? = null,
    onEditListClick: () -> Unit,
    onSortClick: () -> Unit,
    onFilterClick: () -> Unit,
    onClearFiltersClick: () -> Unit,
) {
    var showDropdown by remember { mutableStateOf(false) }

    DropdownMenu(
        visible = showDropdown,
        onDismissRequest = { showDropdown = false },
        anchor = {
            Box {
                IconButton(
                    icon = MdtIcons.More,
                    onClick = { showDropdown = true },
                )

                if (selectedTag != null) {
                    Icon(
                        painter = MdtIcons.CircleFilled,
                        contentDescription = null,
                        tint = MdtTheme.color.notice,
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-6).dp, y = 6.dp),
                    )
                }
            }
        },
        content = {
            DropdownMenuItem(
                text = "Edit list",
                icon = MdtIcons.Edit,
                onClick = {
                    showDropdown = false
                    onEditListClick()
                },
            )

            DropdownMenuItem(
                text = "Sort by",
                icon = MdtIcons.Sort,
                onClick = {
                    showDropdown = false
                    onSortClick()
                },
            )

            DropdownMenuItem(
                text = "Filter",
                icon = MdtIcons.FilterAlt,
                onClick = {
                    showDropdown = false
                    onFilterClick()
                },
            )

            if (selectedTag != null) {
                DropdownMenuItem(
                    text = "Clear filters",
                    icon = MdtIcons.Close,
                    contentColor = MdtTheme.color.error,
                    onClick = {
                        showDropdown = false
                        onClearFiltersClick()
                    },
                )
            }
        },
    )
}