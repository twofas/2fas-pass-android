/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.ktx

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun Instant.formatDate(): String {
    return atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}

fun Instant.formatDateTime(): String {
    val dt = atZone(ZoneId.systemDefault())
    return "${dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))} ${dt.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))}"
}

fun Long.formatDateTime(): String {
    return Instant.ofEpochMilli(this).formatDateTime()
}