/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.testing.navigation

import androidx.lifecycle.SavedStateHandle

fun SavedStateHandle.applyTestArgs(
    gamesVertical: String = "Sport",
    gameId: Long = 0L,
    matchId: Long = 0L,
    competitionId: Long = 0L,
    rewardId: Long = 0L,
    competitionName: String = "Test",
    notificationAckId: String = "Test",
): SavedStateHandle {

    return apply {
        set("gamesVertical", gamesVertical)
        set("gameId", gameId)
        set("matchId", matchId)
        set("competitionId", competitionId)
        set("competitionName", competitionName)
        set("rewardId", rewardId)
        set("notificationAckId", notificationAckId)
        set("webViewPath", "")
        set("dailyGameType", "Daily")
        set("fixtureId", 0L)
        set("openStream", false)
        set("slug", "")
        set("casinoGameId", "")
        set("casinoGameMode", "Demo")
        set("casinoLaunchSource", "GamePage")
        set("phoneDialCode", "")
        set("phoneNumber", "")
        set("kycLevelId", 0L)
        set("kycStepId", 0L)
        set("kycDocumentType", "Other")
        set("racingTab", "NextRaces")
        set("source", "Deeplink")
    }
}