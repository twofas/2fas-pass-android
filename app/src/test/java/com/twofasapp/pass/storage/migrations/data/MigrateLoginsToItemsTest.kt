package com.twofasapp.pass.storage.migrations.data

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.data.main.VaultCipher
import com.twofasapp.data.main.local.model.items.LoginContentEntityV1
import com.twofasapp.pass.storage.AppDatabase
import com.twofasapp.testing.RobolectricTest
import com.twofasapp.testing.coroutines.TestDispatchers
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.util.encodeBase64
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import kotlin.test.Test

class MigrateLoginsToItemsTest : RobolectricTest() {
    private lateinit var appDatabase: AppDatabase
    private lateinit var sqliteDb: SupportSQLiteDatabase
    private lateinit var tested: MigrateLoginsToItems
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        appDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        sqliteDb = appDatabase.openHelper.writableDatabase

        sqliteDb.execSQL(
            """
            CREATE TABLE IF NOT EXISTS vaults (
                id TEXT PRIMARY KEY,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                name TEXT NOT NULL
            )
            """.trimIndent(),
        )

        sqliteDb.execSQL(
            """
            INSERT INTO vaults (id, created_at, updated_at, name)
            VALUES ('vault', 0, 0, 'Test Vault')
            """.trimIndent(),
        )

        sqliteDb.execSQL(
            """
            CREATE TABLE IF NOT EXISTS logins (
                id TEXT PRIMARY KEY,
                vault_id TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                deleted_at INTEGER,
                deleted INTEGER NOT NULL,
                name TEXT NOT NULL,
                username TEXT,
                password TEXT,
                security_type INTEGER NOT NULL,
                uris TEXT,
                icon_type INTEGER NOT NULL,
                icon_uri_index INTEGER,
                custom_image_url TEXT,
                label_text TEXT,
                label_color TEXT,
                notes TEXT,
                tags TEXT
            )
            """.trimIndent(),
        )

        tested = MigrateLoginsToItems(
            dispatchers = TestDispatchers(),
            appDatabase = appDatabase,
            itemsDao = mockk(relaxed = true),
            vaultCryptoScope = mockk(relaxed = true),
            json = Json.Default,
        )
    }

    @After
    fun tearDown() {
        appDatabase.close()
    }

    @Test
    fun `selectLogins returns empty list when no rows`() {
        val result = tested.selectLogins(sqliteDb)

        result.shouldBeEmpty()
    }

    @Test
    fun `selectLogins maps single row with all fields set`() {
        val base64 = "dGVzdA==" // "test"
        val joinedList = listOf("one", "two", "three").joinToString("«§»")

        sqliteDb.execSQL(
            """
            INSERT INTO logins (
                id, vault_id, created_at, updated_at, deleted_at, deleted,
                name, username, password, security_type,
                uris, icon_type, icon_uri_index,
                custom_image_url, label_text, label_color, notes, tags
            ) VALUES (
                '1', 'vault', 1000, 2000, NULL, 0,
                '$base64', '$base64', '$base64', 1,
                '$joinedList', 2, 1,
                '$base64', '$base64', '#ff0000', '$base64', '$joinedList'
            )
            """.trimIndent(),
        )

        val result = tested.selectLogins(sqliteDb)
        result.shouldHaveSize(1)

        val login = result.first()
        login.id.shouldBe("1")
        login.vaultId.shouldBe("vault")
        login.name.bytes.decodeToString().shouldBe("test")
        login.uris.shouldBe(listOf("one", "two", "three"))
    }

    @Test
    fun `selectLogins maps multiple rows`() {
        val base64 = "dGVzdA=="
        sqliteDb.execSQL(
            """
        INSERT INTO logins (
            id, vault_id, created_at, updated_at, deleted_at, deleted,
            name, username, password, security_type,
            uris, icon_type, icon_uri_index,
            custom_image_url, label_text, label_color, notes, tags
        ) VALUES 
        ('1', 'vault', 1, 1, NULL, 0, '$base64', NULL, NULL, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL),
        ('2', 'vault', 2, 2, NULL, 1, '$base64', NULL, NULL, 1, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL)
            """.trimIndent(),
        )

        val result = tested.selectLogins(sqliteDb)
        result.shouldHaveSize(2)
        result[0].id.shouldBe("1")
        result[1].id.shouldBe("2")
    }

    @Test
    fun `selectLogins handles all nullable fields as null`() {
        val base64 = "dGVzdA=="
        sqliteDb.execSQL(
            """
        INSERT INTO logins (
            id, vault_id, created_at, updated_at, deleted_at, deleted,
            name, username, password, security_type,
            uris, icon_type, icon_uri_index,
            custom_image_url, label_text, label_color, notes, tags
        ) VALUES (
            'null-test', 'vault', 1, 1, NULL, 0,
            '$base64', NULL, NULL, 1,
            NULL, 0, NULL,
            NULL, NULL, NULL, NULL, NULL
        )
            """.trimIndent(),
        )

        val result = tested.selectLogins(sqliteDb)
        result.shouldHaveSize(1)

        val login = result.first()
        login.username.shouldBe(null)
        login.password.shouldBe(null)
        login.iconUriIndex.shouldBe(null)
        login.uris.shouldBe(null)
        login.notes.shouldBe(null)
        login.tags.shouldBe(null)
        login.labelColor.shouldBe(null)
    }

    @Test
    fun `selectLogins returns empty list if logins table is empty`() {
        val result = tested.selectLogins(sqliteDb)
        result.shouldBeEmpty()
    }

    @Test
    fun `selectLogins decodes EncryptedBytes from base64`() {
        val base64 = "dGVzdDEyMw==" // "test123"
        sqliteDb.execSQL(
            """
        INSERT INTO logins (
            id, vault_id, created_at, updated_at, deleted_at, deleted,
            name, username, password, security_type,
            uris, icon_type, icon_uri_index,
            custom_image_url, label_text, label_color, notes, tags
        ) VALUES (
            'encrypted', 'vault', 1, 1, NULL, 0,
            '$base64', '$base64', '$base64', 1,
            NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL
        )
            """.trimIndent(),
        )

        val result = tested.selectLogins(sqliteDb)
        val login = result.first()
        login.name.bytes.decodeToString().shouldBe("test123")
        login.username?.bytes?.decodeToString().shouldBe("test123")
        login.password?.bytes?.decodeToString().shouldBe("test123")
    }

    @Test
    fun `mapLoginToItem maps login content json correctly for Tier1`() {
        val json = Json.Default

        val name = EncryptedBytes("n".toByteArray())
        val username = EncryptedBytes("u".toByteArray())
        val customImageUrl = EncryptedBytes("img".toByteArray())
        val labelText = EncryptedBytes("label".toByteArray())
        val notes = EncryptedBytes("notes".toByteArray())

        val expectedContent = LoginContentEntityV1(
            name = "name",
            username = "username",
            password = EncryptedBytes("p".toByteArray()),
            uris = emptyList(),
            iconType = 1,
            iconUriIndex = 2,
            labelText = "label",
            labelColor = "#fff",
            customImageUrl = "img",
            notes = "notes",
        )

        val vaultCipher = mockk<VaultCipher> {
            every { decryptWithSecretKey(name) } returns "name"
            every { decryptWithSecretKey(username) } returns "username"
            every { decryptWithSecretKey(customImageUrl) } returns "img"
            every { decryptWithSecretKey(labelText) } returns "label"
            every { decryptWithSecretKey(notes) } returns "notes"
            every { encryptWithSecretKey(any()) } answers { EncryptedBytes(firstArg<String>().toByteArray()) }
        }

        val login = MigrateLoginsToItems.LoginEntity(
            id = "tier1",
            vaultId = "vault",
            createdAt = 1,
            updatedAt = 2,
            deletedAt = null,
            deleted = false,
            name = name,
            username = username,
            password = EncryptedBytes("p".toByteArray()),
            securityType = 0,
            uris = emptyList(),
            iconType = 1,
            iconUriIndex = 2,
            customImageUrl = customImageUrl,
            labelText = labelText,
            labelColor = "#fff",
            notes = notes,
            tags = emptyList(),
        )

        val result = tested.mapLoginToItem(vaultCipher, login)
        val decoded = json.decodeFromString<LoginContentEntityV1>(result.content.bytes.decodeToString())
        decoded shouldBe expectedContent
    }

    @Test
    fun `mapLoginToItem maps login content json correctly for Tier2`() {
        val json = Json.Default

        val name = EncryptedBytes("n".toByteArray())
        val username = EncryptedBytes("u".toByteArray())
        val customImageUrl = EncryptedBytes("img".toByteArray())
        val labelText = EncryptedBytes("label".toByteArray())
        val notes = EncryptedBytes("notes".toByteArray())

        val expectedContent = LoginContentEntityV1(
            name = "name",
            username = "username",
            password = EncryptedBytes("p".toByteArray()),
            uris = emptyList(),
            iconType = 2,
            iconUriIndex = 3,
            labelText = "label",
            labelColor = "#abc",
            customImageUrl = "img",
            notes = "notes",
        )

        val vaultCipher = mockk<VaultCipher> {
            every { decryptWithTrustedKey(name) } returns "name"
            every { decryptWithTrustedKey(username) } returns "username"
            every { decryptWithTrustedKey(customImageUrl) } returns "img"
            every { decryptWithTrustedKey(labelText) } returns "label"
            every { decryptWithTrustedKey(notes) } returns "notes"
            every { encryptWithTrustedKey(any()) } answers { EncryptedBytes(firstArg<String>().toByteArray()) }
        }

        val login = MigrateLoginsToItems.LoginEntity(
            id = "tier2",
            vaultId = "vault",
            createdAt = 11,
            updatedAt = 22,
            deletedAt = null,
            deleted = false,
            name = name,
            username = username,
            password = EncryptedBytes("p".toByteArray()),
            securityType = 1,
            uris = emptyList(),
            iconType = 2,
            iconUriIndex = 3,
            customImageUrl = customImageUrl,
            labelText = labelText,
            labelColor = "#abc",
            notes = notes,
            tags = emptyList(),
        )

        val result = tested.mapLoginToItem(vaultCipher, login)
        val decoded = json.decodeFromString<LoginContentEntityV1>(result.content.bytes.decodeToString())
        decoded shouldBe expectedContent
    }

    @Test
    fun `mapLoginToItem maps login content json correctly for Tier3`() {
        val json = Json.Default

        val name = EncryptedBytes("n".toByteArray())
        val username = EncryptedBytes("u".toByteArray())
        val customImageUrl = EncryptedBytes("img".toByteArray())
        val labelText = EncryptedBytes("label".toByteArray())
        val notes = EncryptedBytes("notes".toByteArray())

        val expectedContent = LoginContentEntityV1(
            name = "name",
            username = "username",
            password = EncryptedBytes("p".toByteArray()),
            uris = emptyList(),
            iconType = 3,
            iconUriIndex = 4,
            labelText = "label",
            labelColor = "#321",
            customImageUrl = "img",
            notes = "notes",
        )

        val vaultCipher = mockk<VaultCipher> {
            every { decryptWithTrustedKey(name) } returns "name"
            every { decryptWithTrustedKey(username) } returns "username"
            every { decryptWithTrustedKey(customImageUrl) } returns "img"
            every { decryptWithTrustedKey(labelText) } returns "label"
            every { decryptWithTrustedKey(notes) } returns "notes"
            every { encryptWithTrustedKey(any()) } answers { EncryptedBytes(firstArg<String>().toByteArray()) }
        }

        val login = MigrateLoginsToItems.LoginEntity(
            id = "tier3",
            vaultId = "vault",
            createdAt = 100,
            updatedAt = 200,
            deletedAt = null,
            deleted = false,
            name = name,
            username = username,
            password = EncryptedBytes("p".toByteArray()),
            securityType = 2,
            uris = emptyList(),
            iconType = 3,
            iconUriIndex = 4,
            customImageUrl = customImageUrl,
            labelText = labelText,
            labelColor = "#321",
            notes = notes,
            tags = emptyList(),
        )

        val result = tested.mapLoginToItem(vaultCipher, login)
        val decoded = json.decodeFromString<LoginContentEntityV1>(result.content.bytes.decodeToString())
        decoded shouldBe expectedContent
    }

    @Test
    fun `mapLoginToItem parses and decrypts multiple login uris json`() {
        val originalText1 = "test123"
        val originalText2 = "another"
        val encodedText1 = originalText1.encodeBase64()
        val encodedText2 = originalText2.encodeBase64()

        val matcher1 = 5
        val matcher2 = 8

        val uriJson1 = """{"text":"$encodedText1","matcher":"$matcher1"}"""
        val uriJson2 = """{"text":"$encodedText2","matcher":"$matcher2"}"""

        val vaultCipher = mockk<VaultCipher> {
            every { decryptWithTrustedKey(match<EncryptedBytes> { it.bytes.contentEquals(originalText1.toByteArray()) }) } returns originalText1
            every { decryptWithTrustedKey(match<EncryptedBytes> { it.bytes.contentEquals(originalText2.toByteArray()) }) } returns originalText2
            every { decryptWithTrustedKey(match<EncryptedBytes> { it.bytes.contentEquals("dummy".toByteArray()) }) } returns "dummy"
            every { encryptWithTrustedKey(any()) } answers { EncryptedBytes((firstArg<String>()).toByteArray()) }
        }

        val login = MigrateLoginsToItems.LoginEntity(
            id = "uri",
            vaultId = "vault",
            createdAt = 1,
            updatedAt = 1,
            deletedAt = null,
            deleted = false,
            name = EncryptedBytes("dummy".toByteArray()),
            username = null,
            password = null,
            securityType = 2, // Tier 3
            uris = listOf(uriJson1, uriJson2),
            iconType = 0,
            iconUriIndex = null,
            customImageUrl = null,
            labelText = null,
            labelColor = null,
            notes = null,
            tags = emptyList(),
        )

        val result = tested.mapLoginToItem(vaultCipher, login)
        val decoded = Json.decodeFromString<LoginContentEntityV1>(result.content.bytes.decodeToString())

        decoded.uris.shouldHaveSize(2)

        decoded.uris[0].text shouldBe originalText1
        decoded.uris[0].matcher shouldBe matcher1

        decoded.uris[1].text shouldBe originalText2
        decoded.uris[1].matcher shouldBe matcher2
    }
}