package com.twofasapp.feature.itemform

import com.twofasapp.core.common.domain.items.Item

interface ItemFormListener {
    fun onItemUpdated(item: Item) {}
    fun onIsValidUpdated(valid: Boolean) {}
    fun onHasUnsavedChangesUpdated(hasUnsavedChanges: Boolean) {}
    fun onCloseWithoutSaving() {}
}