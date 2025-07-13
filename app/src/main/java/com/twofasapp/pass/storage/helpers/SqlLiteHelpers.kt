package com.twofasapp.pass.storage.helpers

import androidx.sqlite.db.SupportSQLiteDatabase

internal fun SupportSQLiteDatabase.tableExists(table: String): Boolean {
    val cursor = query(
        query = "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
        bindArgs = arrayOf(table),
    )
    val exists = cursor.count > 0
    cursor.close()
    return exists
}

internal fun SupportSQLiteDatabase.countRows(tableName: String): Int {
    if (tableExists(tableName).not()) {
        return 0
    }

    val cursor = this.query("SELECT COUNT(*) FROM $tableName")
    cursor.moveToFirst()
    val count = cursor.getInt(0)
    cursor.close()
    return count
}