package com.twofasapp.feature.quicksetup.ui

import com.twofasapp.core.common.domain.SecurityType

internal data class QuickSetupUiState(
    val syncEnabled: Boolean = false,
    val securityType: SecurityType = SecurityType.Tier3,
)