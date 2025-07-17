package com.twofasapp.pass.storage.migrations.data

import androidx.annotation.VisibleForTesting
import androidx.sqlite.db.SupportSQLiteDatabase
import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.data.main.VaultCipher
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.local.dao.ItemsDao
import com.twofasapp.data.main.local.model.ItemEntity
import com.twofasapp.data.main.local.model.items.LoginContentEntityV1
import com.twofasapp.pass.storage.AppDatabase
import com.twofasapp.pass.storage.helpers.countRows
import com.twofasapp.pass.storage.helpers.tableExists
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber

/**
 * Migration from schema version 1 to 2.
 *
 * This migration copies all entries from the legacy `logins` table
 * (previously represented by `LoginEntity`) into the new `items` table (`ItemEntity`).
 *
 * Migration steps:
 * - Checks if the `logins` table exists (to avoid re-processing on fresh installs).
 * - Reads and maps all legacy login records into `ItemEntity` format.
 * - Persists them via `ItemsDao`.
 * - Verifies that the number of inserted items matches the number of source records.
 * - Drops the old `logins` table after a successful migration.
 *
 * If the row counts do not match, the migration will fail to ensure data integrity.
 */
class MigrateLoginsToItems(
    private val dispatchers: Dispatchers,
    private val appDatabase: AppDatabase,
    private val itemsDao: ItemsDao,
    private val vaultCryptoScope: VaultCryptoScope,
    private val json: Json,
) {

    companion object {
        private const val Tag = "MigrateLoginsToItems"
        private const val LoginsTableName = "logins"
        private const val ItemsTableName = "items"
    }

    suspend fun execute() {
        Timber.tag(Tag).d("Migration started")

        withContext(dispatchers.io) {
            val db = appDatabase.openHelper.writableDatabase

            if (db.tableExists(LoginsTableName).not()) {
                return@withContext
            }

            db.beginTransaction()

            try {
                val logins = selectLogins(db)

                Timber.tag(Tag).d("Found ${logins.size} legacy logins to migrate")

                if (logins.isEmpty()) {
                    db.setTransactionSuccessful()
                    return@withContext
                }

                val vaultCipher = vaultCryptoScope.getVaultCipher(logins.first().vaultId)

                logins
                    .chunked(500)
                    .forEach { chunk ->
                        itemsDao.save(
                            chunk.map { loginEntity ->
                                mapLoginToItem(vaultCipher = vaultCipher, login = loginEntity)
                            },
                        )
                    }

                val countItems = db.countRows(ItemsTableName)

                Timber.tag(Tag).d("Saved $countItems items")

                if (countItems != logins.size) {
                    throw IllegalStateException("Item migration mismatch: inserted ${logins.size}, but only $countItems appear in table.")
                }

                Timber.tag(Tag).d("Migration completed")
                db.setTransactionSuccessful()
                db.endTransaction()
                db.execSQL("DROP TABLE IF EXISTS $LoginsTableName")
            } catch (e: Exception) {
                db.endTransaction()
                Timber.tag(Tag).d("Migration failed ${e.message}")
                throw e
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.Companion.PRIVATE)
    fun selectLogins(sqliteDb: SupportSQLiteDatabase): List<LoginEntity> {
        val cursor = sqliteDb.query(
            """
            SELECT
                id,
                vault_id,
                created_at,
                updated_at,
                deleted_at,
                deleted,
                name,
                username,
                password,
                security_type,
                uris,
                icon_type,
                icon_uri_index,
                custom_image_url,
                label_text,
                label_color,
                notes,
                tags
            FROM $LoginsTableName
            """.trimIndent(),
        )

        val results = mutableListOf<LoginEntity>()

        while (cursor.moveToNext()) {
            val id = cursor.getString(0)
            val vaultId = cursor.getString(1)
            val createdAt = cursor.getLong(2)
            val updatedAt = cursor.getLong(3)
            val deletedAt = if (cursor.isNull(4)) null else cursor.getLong(4)
            val deleted = cursor.getInt(5) != 0

            fun blobFromBase64String(text: String?): EncryptedBytes? =
                text?.let { EncryptedBytes(it.decodeBase64()) }

            fun parseStringList(text: String?): List<String>? =
                if (text.isNullOrBlank()) null else text.split("«§»")

            val name = blobFromBase64String(cursor.getString(6))!!
            val username = blobFromBase64String(cursor.getString(7))
            val password = blobFromBase64String(cursor.getString(8))
            val securityType = cursor.getInt(9)
            val uris = parseStringList(cursor.getString(10))
            val iconType = cursor.getInt(11)
            val iconUriIndex = if (cursor.isNull(12)) null else cursor.getInt(12)
            val customImageUrl = blobFromBase64String(cursor.getString(13))
            val labelText = blobFromBase64String(cursor.getString(14))
            val labelColor = if (cursor.isNull(15)) null else cursor.getString(15)
            val notes = blobFromBase64String(cursor.getString(16))
            val tags = parseStringList(cursor.getString(17))

            results.add(
                LoginEntity(
                    id = id,
                    vaultId = vaultId,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                    deletedAt = deletedAt,
                    deleted = deleted,
                    name = name,
                    username = username,
                    password = password,
                    securityType = securityType,
                    uris = uris,
                    iconType = iconType,
                    iconUriIndex = iconUriIndex,
                    customImageUrl = customImageUrl,
                    labelText = labelText,
                    labelColor = labelColor,
                    notes = notes,
                    tags = tags,
                ),
            )
        }

        cursor.close()
        return results
    }

    @VisibleForTesting(otherwise = VisibleForTesting.Companion.PRIVATE)
    fun mapLoginToItem(
        vaultCipher: VaultCipher,
        login: LoginEntity,
    ): ItemEntity {
        val securityType = when (login.securityType) {
            0 -> SecurityType.Tier1
            1 -> SecurityType.Tier2
            2 -> SecurityType.Tier3
            else -> SecurityType.Tier3
        }

        val content = LoginContentEntityV1(
            name = when (securityType) {
                SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(login.name)
                SecurityType.Tier2 -> vaultCipher.decryptWithTrustedKey(login.name)
                SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(login.name)
            },
            username = login.username?.let { username ->
                when (securityType) {
                    SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(username)
                    SecurityType.Tier2 -> vaultCipher.decryptWithTrustedKey(username)
                    SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(username)
                }
            },
            password = login.password,
            uris = login.uris.orEmpty().map { encryptedLoginJson ->
                val jsonObject = Json.parseToJsonElement(encryptedLoginJson).jsonObject

                val textEncrypted = EncryptedBytes(
                    jsonObject["text"]?.jsonPrimitive?.content.orEmpty().decodeBase64(),
                )

                val matcher = jsonObject["matcher"]?.jsonPrimitive?.content?.toInt()

                LoginContentEntityV1.UriJson(
                    text = when (securityType) {
                        SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(textEncrypted)
                        SecurityType.Tier2 -> vaultCipher.decryptWithTrustedKey(textEncrypted)
                        SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(textEncrypted)
                    },
                    matcher = matcher ?: 0,
                )
            },
            iconType = login.iconType,
            iconUriIndex = login.iconUriIndex,
            labelText = login.labelText?.let { labelText ->
                when (securityType) {
                    SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(labelText)
                    SecurityType.Tier2 -> vaultCipher.decryptWithTrustedKey(labelText)
                    SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(labelText)
                }
            },
            labelColor = login.labelColor,
            customImageUrl = login.customImageUrl?.let { customImageUrl ->
                when (securityType) {
                    SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(customImageUrl)
                    SecurityType.Tier2 -> vaultCipher.decryptWithTrustedKey(customImageUrl)
                    SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(customImageUrl)
                }
            },
            notes = login.notes?.let { notes ->
                when (securityType) {
                    SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(notes)
                    SecurityType.Tier2 -> vaultCipher.decryptWithTrustedKey(notes)
                    SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(notes)
                }
            },
        )

        val contentJson = json.encodeToString<LoginContentEntityV1>(content)

        val contentEncrypted = when (securityType) {
            SecurityType.Tier1 -> vaultCipher.encryptWithSecretKey(contentJson)
            SecurityType.Tier2 -> vaultCipher.encryptWithTrustedKey(contentJson)
            SecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey(contentJson)
        }

        return ItemEntity(
            id = login.id,
            vaultId = login.vaultId,
            createdAt = login.createdAt,
            updatedAt = login.updatedAt,
            deletedAt = login.deletedAt,
            deleted = login.deleted,
            securityType = login.securityType,
            contentType = content.contentType,
            contentVersion = content.contentVersion,
            content = contentEncrypted,
            tagIds = login.tags,
        )
    }

    data class LoginEntity(
        val id: String,
        val vaultId: String,
        val createdAt: Long,
        val updatedAt: Long,
        val deletedAt: Long?,
        val deleted: Boolean,
        val name: EncryptedBytes,
        val username: EncryptedBytes?,
        val password: EncryptedBytes?,
        val securityType: Int,
        val uris: List<String>?,
        val iconType: Int,
        val iconUriIndex: Int?,
        val customImageUrl: EncryptedBytes?,
        val labelText: EncryptedBytes?,
        val labelColor: String?,
        val notes: EncryptedBytes?,
        val tags: List<String>?,
    )
}