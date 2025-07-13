package com.twofasapp.pass.storage.helpers

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import com.twofasapp.pass.storage.AppDatabase
import com.twofasapp.testing.RobolectricTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Test

class SqlLiteHelpersTest : RobolectricTest() {
    private lateinit var appDatabase: AppDatabase
    private lateinit var sqliteDb: SupportSQLiteDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        appDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        sqliteDb = appDatabase.openHelper.writableDatabase
    }

    @After
    fun tearDown() {
        appDatabase.close()
    }

    @Test
    fun `verify table exists`() {
        sqliteDb.execSQL("CREATE TABLE test_table (id TEXT PRIMARY KEY)")

        sqliteDb.tableExists("test_table").shouldBeTrue()
    }

    @Test
    fun `verify table does not exist`() {
        sqliteDb.tableExists("non_existing_table").shouldBeFalse()
    }

    @Test
    fun `verify table does not exist after drop`() {
        sqliteDb.execSQL("CREATE TABLE test_table (id TEXT PRIMARY KEY)")
        sqliteDb.execSQL("DROP TABLE test_table")

        sqliteDb.tableExists("test_table").shouldBeFalse()
    }

    @Test
    fun `count rows should be 0 for empty table`() {
        sqliteDb.execSQL("CREATE TABLE test_table (id TEXT PRIMARY KEY)")

        sqliteDb.countRows("test_table").shouldBe(0)
    }

    @Test
    fun `verify count rows`() {
        val count = 3
        sqliteDb.execSQL("CREATE TABLE test_table (id TEXT PRIMARY KEY)")

        repeat(count) {
            sqliteDb.execSQL("INSERT INTO test_table (id) VALUES ('$it')")
        }

        sqliteDb.countRows("test_table").shouldBe(count)

        sqliteDb.execSQL("DROP TABLE test_table")

        sqliteDb.countRows("test_table").shouldBe(0)
    }
}