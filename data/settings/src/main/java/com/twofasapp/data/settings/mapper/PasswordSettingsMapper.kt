/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.settings.mapper

import com.twofasapp.core.common.domain.PasswordGeneratorSettings
import com.twofasapp.data.settings.local.model.PasswordGeneratorSettingsEntity

internal fun PasswordGeneratorSettingsEntity.asDomain(): PasswordGeneratorSettings {
    return PasswordGeneratorSettings(
        length = length,
        requireDigits = requireDigits,
        requireUppercase = requireUppercase,
        requireSpecial = requireSpecial,
    )
}

internal fun PasswordGeneratorSettings.asEntity(): PasswordGeneratorSettingsEntity {
    return PasswordGeneratorSettingsEntity(
        length = length,
        requireDigits = requireDigits,
        requireUppercase = requireUppercase,
        requireSpecial = requireSpecial,
    )
}