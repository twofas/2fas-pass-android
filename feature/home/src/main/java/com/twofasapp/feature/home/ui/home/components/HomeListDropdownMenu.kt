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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.menu.DropdownMenu
import com.twofasapp.core.design.foundation.menu.DropdownMenuItem
import com.twofasapp.core.locale.MdtLocale

@Composable
internal fun HomeListDropdownMenu(
    editVisible: Boolean = true,
    selectedTag: Tag? = null,
    onEditListClick: () -> Unit = {},
    onSortClick: () -> Unit,
    onFilterClick: () -> Unit,
    onClearFiltersClick: () -> Unit,
) {
    var showDropdown by remember { mutableStateOf(false) }
    val strings = MdtLocale.strings

    DropdownMenu(
        visible = showDropdown,
        onDismissRequest = { showDropdown = false },
        anchor = {
            Box {
                IconButton(
                    modifier = Modifier.testTag("homeListMenuButton"),
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
            if (editVisible) {
                DropdownMenuItem(
                    text = strings.homeListMenuEdit,
                    leadingIcon = MdtIcons.Edit,
                    onClick = {
                        showDropdown = false
                        onEditListClick()
                    },
                )
            }

            DropdownMenuItem(
                text = strings.homeListMenuSort,
                leadingIcon = MdtIcons.Sort,
                onClick = {
                    showDropdown = false
                    onSortClick()
                },
            )

            DropdownMenuItem(
                text = strings.homeListMenuFilter,
                leadingIcon = MdtIcons.FilterAlt,
                onClick = {
                    showDropdown = false
                    onFilterClick()
                },
            )

            if (selectedTag != null) {
                DropdownMenuItem(
                    text = strings.homeListMenuClearFilters,
                    leadingIcon = MdtIcons.Close,
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