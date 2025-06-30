/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.settings.mapper

import com.twofasapp.core.common.domain.SelectedTheme
import com.twofasapp.data.settings.domain.LoginClickAction
import com.twofasapp.data.settings.domain.SortingMethod
import com.twofasapp.data.settings.local.model.LoginClickActionEntity
import com.twofasapp.data.settings.local.model.SelectedThemeEntity
import com.twofasapp.data.settings.local.model.SortingMethodEntity

internal fun LoginClickActionEntity.asDomain(): LoginClickAction {
    return when (this) {
        LoginClickActionEntity.View -> LoginClickAction.View
        LoginClickActionEntity.Edit -> LoginClickAction.Edit
        LoginClickActionEntity.CopyPassword -> LoginClickAction.CopyPassword
        LoginClickActionEntity.OpenUri -> LoginClickAction.OpenUri
    }
}

internal fun LoginClickAction.asEntity(): LoginClickActionEntity {
    return when (this) {
        LoginClickAction.View -> LoginClickActionEntity.View
        LoginClickAction.Edit -> LoginClickActionEntity.Edit
        LoginClickAction.CopyPassword -> LoginClickActionEntity.CopyPassword
        LoginClickAction.OpenUri -> LoginClickActionEntity.OpenUri
    }
}

internal fun SelectedThemeEntity.asDomain(): SelectedTheme {
    return when (this) {
        SelectedThemeEntity.Light -> SelectedTheme.Light
        SelectedThemeEntity.Dark -> SelectedTheme.Dark
        SelectedThemeEntity.Auto -> SelectedTheme.Auto
    }
}

internal fun SelectedTheme.asEntity(): SelectedThemeEntity {
    return when (this) {
        SelectedTheme.Light -> SelectedThemeEntity.Light
        SelectedTheme.Dark -> SelectedThemeEntity.Dark
        SelectedTheme.Auto -> SelectedThemeEntity.Auto
    }
}

internal fun SortingMethodEntity.asDomain(): SortingMethod {
    return when (this) {
        SortingMethodEntity.NameAsc -> SortingMethod.NameAsc
        SortingMethodEntity.NameDesc -> SortingMethod.NameDesc
        SortingMethodEntity.CreationDateAsc -> SortingMethod.CreationDateAsc
        SortingMethodEntity.CreationDateDesc -> SortingMethod.CreationDateDesc
    }
}

internal fun SortingMethod.asEntity(): SortingMethodEntity {
    return when (this) {
        SortingMethod.NameAsc -> SortingMethodEntity.NameAsc
        SortingMethod.NameDesc -> SortingMethodEntity.NameDesc
        SortingMethod.CreationDateAsc -> SortingMethodEntity.CreationDateAsc
        SortingMethod.CreationDateDesc -> SortingMethodEntity.CreationDateDesc
    }
}