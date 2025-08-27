package com.twofasapp.core.design.feature.tags

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.foundation.dialog.InputDialog
import com.twofasapp.core.design.foundation.dialog.InputValidation
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.locale.MdtLocale

@Composable
fun TagDialog(
    onDismissRequest: () -> Unit,
    tag: Tag,
    onSaveClick: (Tag) -> Unit = {},
) {
    InputDialog(
        onDismissRequest = onDismissRequest,
        title = if (tag.id.isEmpty()) {
            MdtLocale.strings.tagEditorNewTitle
        } else {
            MdtLocale.strings.tagEditorEditTitle
        },
        body = MdtLocale.strings.tagEditorDescription,
        icon = if (tag.id.isEmpty()) {
            MdtIcons.AddTag
        } else {
            MdtIcons.Tag
        },
        prefill = tag.name,
        label = MdtLocale.strings.tagEditorPlaceholder,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
        ),
        onPositive = { text ->
            onSaveClick(
                tag.copy(
                    name = text.trim(),
                ),
            )
        },
        validate = { text ->
            if (text.isBlank()) {
                InputValidation.Invalid("Name can not be empty")
            } else if (text.length > 1000) {
                InputValidation.Invalid("Max length is 1000 characters")
            } else {
                InputValidation.Valid
            }
        },
    )
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        TagDialog(
            onDismissRequest = {},
            tag = Tag.Empty,
        )
    }
}