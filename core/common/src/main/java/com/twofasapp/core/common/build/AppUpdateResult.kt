package com.twofasapp.core.common.build

sealed interface AppUpdateResult {
    data object Completed : AppUpdateResult
    data class Failed(val error: Throwable) : AppUpdateResult
}