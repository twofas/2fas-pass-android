package com.twofasapp.feature.itemform

data class ItemFormProperties(
    val shouldConfirmUnsavedChanges: Boolean,
) {
    companion object {
        val Default = ItemFormProperties(
            shouldConfirmUnsavedChanges = true,
        )
    }
}