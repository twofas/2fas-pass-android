/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.logs

import androidx.lifecycle.ViewModel
import com.opencsv.CSVWriter
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.data.logs.LogsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.io.StringWriter
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

internal class LogsViewModel(
    private val logsRepository: LogsRepository,
    appBuild: AppBuild,
) : ViewModel() {
    val uiState = MutableStateFlow(LogsUiState(isDebuggable = appBuild.debuggable))

    init {
        loadLogs()
    }

    private fun loadLogs() {
        launchScoped {
            val logs = logsRepository.getAll()
            uiState.update { it.copy(logs = logs, isLoading = false) }
        }
    }

    fun generateShareContent(): String {
        val logs = uiState.value.logs
        val stringWriter = StringWriter()
        CSVWriter(
            /* writer = */
            stringWriter,
            /* separator = */
            CSVWriter.DEFAULT_SEPARATOR,
            /* quotechar = */
            CSVWriter.NO_QUOTE_CHARACTER,
            /* escapechar = */
            CSVWriter.DEFAULT_ESCAPE_CHARACTER,
            /* lineEnd = */
            CSVWriter.DEFAULT_LINE_END,
        ).use { csvWriter ->
            val utcFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            csvWriter.writeNext(arrayOf("timestamp", "tag", "message"))
            logs.forEach { entry ->
                val utcTime = Instant.ofEpochMilli(entry.timestamp).atZone(ZoneOffset.UTC).format(utcFormatter)
                csvWriter.writeNext(arrayOf(utcTime, entry.tag, entry.message))
            }
        }
        return stringWriter.toString()
    }

    fun clearLogs() {
        launchScoped {
            logsRepository.deleteAll()
            uiState.update { it.copy(logs = emptyList()) }
        }
    }

    fun generateFilename(): String {
        return "2FAS_Pass_Logs_${
            Instant.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        }.txt"
    }
}