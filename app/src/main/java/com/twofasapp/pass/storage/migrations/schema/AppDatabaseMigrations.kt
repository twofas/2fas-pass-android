package com.twofasapp.pass.storage.migrations.schema

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Manual and auto migrations for AppDatabase.
 */
object AppDatabaseMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // No schema change here. Just preserve the `logins` table
            // for later manual migration in code (MigrateLoginsToItems).
        }
    }
}