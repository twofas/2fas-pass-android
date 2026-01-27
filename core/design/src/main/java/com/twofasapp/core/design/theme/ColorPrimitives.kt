/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

internal val seed = Color(0xFF214CE8)

internal val primaryLight = Color(0xFF0037AF)
internal val onPrimaryLight = Color(0xFFFFFFFF)
internal val primaryContainerLight = Color(0xFF265AED)
internal val onPrimaryContainerLight = Color(0xFFFFFFFF)
internal val secondaryLight = Color(0xFF4A5B9A)
internal val onSecondaryLight = Color(0xFFFFFFFF)
internal val secondaryContainerLight = Color(0xFFB3C1FF)
internal val onSecondaryContainerLight = Color(0xFF1E306D)
internal val tertiaryLight = Color(0xFF730093)
internal val onTertiaryLight = Color(0xFFFFFFFF)
internal val tertiaryContainerLight = Color(0xFFFFD7F3)
internal val onTertiaryContainerLight = Color(0xFF2D1228)
internal val errorLight = Color(0xFFBA1A1A)
internal val onErrorLight = Color(0xFFFFFFFF)
internal val errorContainerLight = Color(0xFFFFDAD6)
internal val onErrorContainerLight = Color(0xFF410002)
internal val backgroundLight = Color(0xFFFBF8FF)
internal val onBackgroundLight = Color(0xFF1B1B21)
internal val surfaceLight = Color(0xFFFAF8FF)
internal val onSurfaceLight = Color(0xFF191B24)
internal val surfaceVariantLight = Color(0xFFE3E1EC)
internal val onSurfaceVariantLight = Color(0xFF696B7B)
internal val outlineLight = Color(0xFF747687)
internal val outlineVariantLight = Color(0xFFC3C5D8)
internal val scrimLight = Color(0xFF000000)
internal val inverseSurfaceLight = Color(0xFF2E3039)
internal val inverseOnSurfaceLight = Color(0xFFF0F0FC)
internal val inversePrimaryLight = Color(0xFFB7C4FF)
internal val surfaceDimLight = Color(0xFFD9D9E5)
internal val surfaceBrightLight = Color(0xFFFAF8FF)
internal val surfaceContainerLowestLight = Color(0xFFFFFFFF)
internal val surfaceContainerLowLight = Color(0xFFF3F2FF)
internal val surfaceContainerLight = Color(0xFFEDEDF9)
internal val surfaceContainerHighLight = Color(0xFFE7E7F3)
internal val surfaceContainerHighestLight = Color(0xFFE2E1EE)
internal val successLight = Color(0xFF00B856)

internal val primaryDark = Color(0xFFB7C4FF)
internal val onPrimaryDark = Color(0xFF002681)
internal val primaryContainerDark = Color(0xFF0040C8)
internal val onPrimaryContainerDark = Color(0xFFECEDFF)
internal val secondaryDark = Color(0xFFB7C4FF)
internal val onSecondaryDark = Color(0xFF192C69)
internal val secondaryContainerDark = Color(0xFF2A3B79)
internal val onSecondaryContainerDark = Color(0xFFC9D2FF)
internal val tertiaryDark = Color(0xFFF1AFFF)
internal val onTertiaryDark = Color(0xFF54006D)
internal val tertiaryContainerDark = Color(0xFF8314A4)
internal val onTertiaryContainerDark = Color(0xFFFFE9FF)
internal val errorDark = Color(0xFFFFB4AB)
internal val onErrorDark = Color(0xFF690005)
internal val errorContainerDark = Color(0xFF93000A)
internal val onErrorContainerDark = Color(0xFFFFDAD6)
internal val backgroundDark = Color(0xFF121318)
internal val onBackgroundDark = Color(0xFFE3E1E9)
internal val surfaceDark = Color(0xFF11131B)
internal val onSurfaceDark = Color(0xFFE2E1EE)
internal val surfaceVariantDark = Color(0xFF45464F)
internal val onSurfaceVariantDark = Color(0xFFC3C5D8)
internal val outlineDark = Color(0xFF8D90A1)
internal val outlineVariantDark = Color(0xFF434655)
internal val scrimDark = Color(0xFF000000)
internal val inverseSurfaceDark = Color(0xFFE2E1EE)
internal val inverseOnSurfaceDark = Color(0xFF2E3039)
internal val inversePrimaryDark = Color(0xFF134FE4)
internal val surfaceDimDark = Color(0xFF11131B)
internal val surfaceBrightDark = Color(0xFF373942)
internal val surfaceContainerLowestDark = Color(0xFF0C0E16)
internal val surfaceContainerLowDark = Color(0xFF191B24)
internal val surfaceContainerDark = Color(0xFF1D1F28)
internal val surfaceContainerHighDark = Color(0xFF282933)
internal val surfaceContainerHighestDark = Color(0xFF33343E)
internal val successDark = Color(0xFF00C457)

internal val info500 = Color(0xFF0048DE)
internal val brand500 = Color(0xFF214CE8)
internal val securityItemFilterTint = Color(0xFF0077FF)

internal val LightColors = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

internal val DarkColors = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)