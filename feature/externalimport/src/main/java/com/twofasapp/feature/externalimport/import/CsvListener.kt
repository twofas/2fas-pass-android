package com.twofasapp.feature.externalimport.import

internal interface CsvListener {
    fun onHeaders(headersList: List<String>)
    fun onRow(row: CsvRow)
}