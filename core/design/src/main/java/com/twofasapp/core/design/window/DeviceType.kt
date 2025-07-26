package com.twofasapp.core.design.window

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowWidthSizeClass

enum class DeviceType {
    Compact,
    Medium,
    Expanded,
}

@Composable
fun currentDeviceType(): DeviceType {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return windowSizeClass.windowWidthSizeClass.toDeviceType()
}

private fun WindowWidthSizeClass.toDeviceType(): DeviceType {
    return when (this) {
        WindowWidthSizeClass.COMPACT -> DeviceType.Compact
        WindowWidthSizeClass.MEDIUM -> DeviceType.Medium
        WindowWidthSizeClass.EXPANDED -> DeviceType.Expanded
        else -> DeviceType.Compact // fallback
    }
}