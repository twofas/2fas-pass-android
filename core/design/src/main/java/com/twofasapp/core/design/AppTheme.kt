/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design

import android.content.pm.ActivityInfo
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.twofasapp.core.common.domain.AuthStatus
import com.twofasapp.core.design.theme.ColorTokens
import com.twofasapp.core.design.theme.DarkColors
import com.twofasapp.core.design.theme.LightColors
import com.twofasapp.core.design.theme.seed
import com.twofasapp.core.design.theme.successDark
import com.twofasapp.core.design.theme.successLight
import com.twofasapp.core.design.window.ScreenOrientation

val LocalAppTheme = staticCompositionLocalOf { AppTheme.Auto }
val LocalColorTokens = staticCompositionLocalOf { ColorTokens() }
val LocalDynamicColors = staticCompositionLocalOf { true }
val LocalDarkMode = staticCompositionLocalOf { true }
val LocalAuthStatus = staticCompositionLocalOf<AuthStatus?> { null }

enum class AppTheme {
    Auto, Light, Dark,
}

@Composable
fun AppTheme(
    content: @Composable () -> Unit,
) {
    ScreenOrientation(compactOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

    val isDynamicColorEnabled = LocalDynamicColors.current

    val isInDarkTheme = when (LocalAppTheme.current) {
        AppTheme.Auto -> isSystemInDarkTheme()
        AppTheme.Light -> false
        AppTheme.Dark -> true
    }

    val colorScheme: ColorScheme = when {
        isDynamicColorEnabled && isInDarkTheme -> dynamicDarkColorScheme(LocalContext.current)
        isDynamicColorEnabled && !isInDarkTheme -> dynamicLightColorScheme(LocalContext.current)
        isInDarkTheme -> DarkColors
        else -> LightColors
    }

    val colorTokens = ColorTokens(
        seed = seed,
        primary = colorScheme.primary,
        onPrimary = colorScheme.onPrimary,
        primaryContainer = colorScheme.primaryContainer,
        onPrimaryContainer = colorScheme.onPrimaryContainer,
        secondary = colorScheme.secondary,
        onSecondary = colorScheme.onSecondary,
        secondaryContainer = colorScheme.secondaryContainer,
        onSecondaryContainer = colorScheme.onSecondaryContainer,
        tertiary = colorScheme.tertiary,
        onTertiary = colorScheme.onTertiary,
        tertiaryContainer = colorScheme.tertiaryContainer,
        onTertiaryContainer = colorScheme.onTertiaryContainer,
        error = colorScheme.error,
        onError = colorScheme.onError,
        errorContainer = colorScheme.errorContainer,
        onErrorContainer = colorScheme.onErrorContainer,
        background = colorScheme.background,
        onBackground = colorScheme.onBackground,
        success = if (isInDarkTheme) successDark else successLight,
        notice = colorScheme.error,
        surface = colorScheme.surface,
        onSurface = colorScheme.onSurface,
        surfaceVariant = colorScheme.surfaceVariant,
        onSurfaceVariant = colorScheme.onSurfaceVariant,
        outline = colorScheme.outline,
        outlineVariant = colorScheme.outlineVariant,
        scrim = colorScheme.scrim,
        inverseSurface = colorScheme.inverseSurface,
        inverseOnSurface = colorScheme.inverseOnSurface,
        inversePrimary = colorScheme.inversePrimary,
        surfaceDim = colorScheme.surfaceDim,
        surfaceBright = colorScheme.surfaceBright,
        surfaceContainerLowest = colorScheme.surfaceContainerLowest,
        surfaceContainerLow = colorScheme.surfaceContainerLow,
        surfaceContainer = colorScheme.surfaceContainer,
        surfaceContainerHigh = colorScheme.surfaceContainerHigh,
        surfaceContainerHighest = colorScheme.surfaceContainerHighest,
        bottomBar = if (isInDarkTheme) colorScheme.background else colorScheme.background,
        itemLoginContent = if (isInDarkTheme) Color(0xFF0088FF) else Color(0xFF0088FF),
        itemLoginContainer = if (isInDarkTheme) Color(0xFF002B52) else Color(0xFFD4EBFF),
        itemSecureNoteContent = if (isInDarkTheme) Color(0xFFFF8D28) else Color(0xFFFF8D28),
        itemSecureNoteContainer = if (isInDarkTheme) Color(0xFF482709) else Color(0xFFFFF1E4),
        itemPaymentCardContent = if (isInDarkTheme) Color(0xFF34C759) else Color(0xFF34C759),
        itemPaymentCardContainer = if (isInDarkTheme) Color(0xFF043B12) else Color(0xFFD6FFE0),
    )

    CompositionLocalProvider(
        LocalColorTokens provides colorTokens,
        LocalDarkMode provides isInDarkTheme,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}