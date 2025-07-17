package com.twofasapp.core.common.build

interface AppUpdateExecutor {
    suspend fun execute(): AppUpdateResult
}