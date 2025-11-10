package com.twofasapp.feature.itemform.forms.common

import com.twofasapp.core.design.foundation.lazy.ListItem

internal sealed class FormListItem(key: Any? = null, type: Any? = null) : ListItem(key, type) {
    data class Field(val name: String) : FormListItem("Field:$$name", "Field")
    data object SecurityTypePicker : FormListItem()
    data object TagsPicker : FormListItem()
    data object TimestampInfo : FormListItem()
}