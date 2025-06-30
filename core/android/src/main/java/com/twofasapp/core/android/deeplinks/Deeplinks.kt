/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.android.deeplinks

import android.app.Activity
import android.content.Intent
import com.twofasapp.core.android.navigation.Screen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface Deeplinks {

    companion object {
        const val Tag = "Deeplinks"

        val Empty = object : Deeplinks {
            override suspend fun onCreate(activity: Activity, intent: Intent) = Unit
            override suspend fun onNewIntent(activity: Activity, intent: Intent) = Unit
            override fun observePendingDeeplink(): Flow<Deeplink> = flowOf()
            override fun clearPendingDeeplink() = Unit
            override fun openScreen(screen: Screen) = Unit
            override fun openScreens(screens: List<Screen>) = Unit
        }
    }

    suspend fun onCreate(activity: Activity, intent: Intent)
    suspend fun onNewIntent(activity: Activity, intent: Intent)
    fun observePendingDeeplink(): Flow<Deeplink>
    fun clearPendingDeeplink()
    fun openScreen(screen: Screen)
    fun openScreens(screens: List<Screen>)
}