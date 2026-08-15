package com.twofasapp.core.design.foundation.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import com.twofasapp.core.android.ktx.toDp
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.layout.ActionsRow
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.textfield.TextField
import com.twofasapp.core.design.foundation.textfield.TextFieldDefaults

@Composable
fun DropdownPicker(
    value: String,
    label: String,
    dropdownVisible: Boolean,
    modifier: Modifier = Modifier,
    testTag: String? = null,
    onShowDropdown: () -> Unit,
    onDismissDropdown: () -> Unit,
    dropdownContent: @Composable ColumnScope.() -> Unit,
) {
    val textMeasurer = rememberTextMeasurer()
    val sizeResult = textMeasurer.measure(label, MdtTheme.typo.bodySmall)
    Box(modifier = modifier) {
        TextField(
            modifier = Modifier
                .fillMaxWidth(),
            readOnly = true,
            value = value,
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
            labelText = label,
            singleLine = true,
            maxLines = 1,
            trailingIcon = {
                ActionsRow(
                    useHorizontalPadding = true,
                ) {
                    IconButton(
                        icon = MdtIcons.ChevronDown,
                    )
                }
            },
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = (sizeResult.size.height.toDp() / 2))
                .clip(TextFieldDefaults.shape)
                .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
                .clickable(onClick = onShowDropdown),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = (sizeResult.size.height.toDp() / 2)),
        ) {
            DropdownMenu(
                shape = TextFieldDefaults.shape,
                visible = dropdownVisible,
                onDismissRequest = onDismissDropdown,
                anchor = {},
                content = dropdownContent,
            )
        }
    }
}

@Preview
@Composable
private fun DropdownPickerPreview() {
    PreviewTheme {
        DropdownPicker(
            value = "value",
            label = "label",
            dropdownVisible = false,
            onShowDropdown = {},
            onDismissDropdown = {},
            dropdownContent = {},
        )
    }
}

@Preview
@Composable
private fun DropdownPickerVisiblePreview() {
    PreviewTheme {
        DropdownPicker(
            value = "value",
            label = "label",
            dropdownVisible = true,
            onShowDropdown = {},
            onDismissDropdown = {},
            dropdownContent = {
                Text("item 1")
                Text("item 2")
                Text("item 3")
            },
        )
    }
}