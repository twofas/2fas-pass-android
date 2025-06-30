/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.foundation.progress

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant

enum class MdtTimerResolution {
    SECONDS, MINUTES, HOURS,
}

@Composable
fun Timer(
    interval: Long = 1000L,
    onTick: (Instant) -> Unit = {},
) {
    LaunchedEffect(Unit) {
        while (true) {
            onTick(Instant.now())
            delay(interval)
        }
    }
}

@Composable
fun Timer(
    interval: Long = 1000L,
    duration: Long = 1000L,
    onTick: (Instant) -> Unit = {},
    onElapsed: () -> Unit = {},
) {
    var elapsed by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (elapsed < duration) {
            onTick(Instant.now())
            delay(interval)
            elapsed += interval

            if (elapsed == duration) {
                onElapsed()
            }
        }
    }
}

@Composable
fun TimerDuration(
    instant: Instant,
    interval: Long = 1000L,
    resolution: MdtTimerResolution = MdtTimerResolution.SECONDS,
    onTick: (String) -> Unit = {},
    onTickTimer: (Long) -> Unit = {},
) {
    LaunchedEffect(instant) {
        while (true) {
            val duration = Duration.between(Instant.now(), instant)
            val format = formatTimeLeft(
                duration = duration,
                resolution = resolution,
            )

            onTickTimer(duration.toSeconds())
            onTick(format)
            delay(interval)
        }
    }
}

@Composable
fun TimerCountdown(
    timer: Long,
    interval: Long = 1000L,
    onTick: (Long) -> Unit = {},
) {
    var timeLeft by rememberSaveable { mutableLongStateOf(timer) }

    LaunchedEffect(timeLeft) {
        while (timeLeft > 0) {
            delay(interval)
            timeLeft -= interval
            onTick(timeLeft)
        }
    }
}

fun formatTimeLeft(
    duration: Duration,
    resolution: MdtTimerResolution = MdtTimerResolution.SECONDS,
): String {
    return buildString {
        if (duration.toHours() > 0) {
            append("${duration.toHours()}h")

            if (resolution == MdtTimerResolution.MINUTES || resolution == MdtTimerResolution.SECONDS) {
                append(" ${duration.toMinutesPart()}m")
            }
        } else if (duration.toMinutes() > 0) {
            append("${duration.toMinutes()}m")

            if (resolution == MdtTimerResolution.SECONDS) {
                append(" ${duration.toSecondsPart()}s")
            }
        } else if (duration.toSeconds() > 0) {
            append("${duration.toSeconds()}s")
        } else {
            append("0s")
        }
    }
}