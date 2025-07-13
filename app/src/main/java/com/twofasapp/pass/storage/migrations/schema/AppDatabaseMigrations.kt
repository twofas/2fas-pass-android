package com.twofasapp.pass.storage.migrations.schema

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Manual and auto migrations for AppDatabase.
 */
object AppDatabaseMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
            CREATE TABLE IF NOT EXISTS items (
                id TEXT NOT NULL PRIMARY KEY,
                vault_id TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                deleted_at INTEGER,
                deleted INTEGER NOT NULL,
                security_type INTEGER NOT NULL,
                content_type TEXT NOT NULL,
                content_version INTEGER NOT NULL,
                content TEXT NOT NULL,
                tag_ids TEXT,
                FOREIGN KEY(vault_id) REFERENCES vaults(id) ON DELETE CASCADE ON UPDATE CASCADE
            )
                """.trimIndent(),
            )

            db.execSQL("CREATE INDEX IF NOT EXISTS index_items_vault_id ON items(vault_id)")
        }
    }
}