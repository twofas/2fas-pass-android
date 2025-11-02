package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.data.main.FakeVaultCipher
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test

class UnknownItemEncryptionMapperTest {

    private val json = Json
    private val vaultCipher = FakeVaultCipher()
    private val mapper = UnknownItemEncryptionMapper(json)

    @Test
    fun `encrypt only transforms top level secret string fields`() {
        val raw = """{"name":"Example","s_secret":"topSecret","s_null":null,"count":5}"""

        val encrypted = mapper.encrypt(raw, SecurityType.Tier1, vaultCipher)

        val element = json.parseToJsonElement(encrypted).jsonObject
        element["name"]!!.jsonPrimitive.content shouldBe "Example"
        element["count"]!!.jsonPrimitive.int shouldBe 5
        element["s_null"] shouldBe JsonNull

        val encodedSecret = element["s_secret"]!!.jsonPrimitive.content
        String(encodedSecret.decodeBase64(), Charsets.UTF_8) shouldBe "secret:topSecret"
    }

    @Test
    fun `decrypt restores secret fields when decryption requested`() {
        val raw = """{"plain":"text","s_secret":"value"}"""
        val encrypted = mapper.encrypt(raw, SecurityType.Tier2, vaultCipher)

        val decrypted = mapper.decrypt(encrypted, SecurityType.Tier2, vaultCipher, decryptSecretFields = true) as ItemContent.Unknown
        val decryptedObject = json.parseToJsonElement(decrypted.rawJson).jsonObject

        decryptedObject["plain"]!!.jsonPrimitive.content shouldBe "text"
        decryptedObject["s_secret"]!!.jsonPrimitive.content shouldBe "value"
    }

    @Test
    fun `decrypt returns raw json when secret fields not requested`() {
        val raw = """{"s_secret":"value"}"""
        val encrypted = mapper.encrypt(raw, SecurityType.Tier3, vaultCipher)

        val decrypted = mapper.decrypt(encrypted, SecurityType.Tier3, vaultCipher, decryptSecretFields = false) as ItemContent.Unknown

        decrypted.rawJson shouldBe encrypted
    }

    @Test
    fun `encrypt ignores non string secret fields`() {
        val raw = """{"s_object":{"inner":"value"},"s_list":[1,2,3],"s_string":"text"}"""

        val encrypted = mapper.encrypt(raw, SecurityType.Tier1, vaultCipher)
        val element = json.parseToJsonElement(encrypted).jsonObject

        element["s_object"]!!.jsonObject["inner"]!!.jsonPrimitive.content shouldBe "value"
        element["s_list"]!!.jsonArray.size shouldBe 3
        val encoded = element["s_string"]!!.jsonPrimitive.content
        String(encoded.decodeBase64(), Charsets.UTF_8) shouldBe "secret:text"
    }

    @Test
    fun `encryptSecretFields encrypts secret fields with provided key`() {
        val raw = """{"name":"Example","s_secret":"topSecret","count":5}"""
        val encryptionKey = ByteArray(32) { it.toByte() }

        val result = mapper.encryptSecretFields(raw, encryptionKey)

        val element = json.parseToJsonElement(result.rawJson).jsonObject
        element["name"]!!.jsonPrimitive.content shouldBe "Example"
        element["count"]!!.jsonPrimitive.int shouldBe 5

        val encodedSecret = element["s_secret"]!!.jsonPrimitive.content
        encodedSecret.decodeBase64().isNotEmpty() shouldBe true
    }

    @Test
    fun `encryptSecretFields does not encrypt non-secret fields`() {
        val raw = """{"name":"Example","email":"test@example.com","s_password":"secret123"}"""
        val encryptionKey = ByteArray(32) { it.toByte() }

        val result = mapper.encryptSecretFields(raw, encryptionKey)

        val element = json.parseToJsonElement(result.rawJson).jsonObject
        element["name"]!!.jsonPrimitive.content shouldBe "Example"
        element["email"]!!.jsonPrimitive.content shouldBe "test@example.com"

        val encodedPassword = element["s_password"]!!.jsonPrimitive.content
        encodedPassword shouldNotBe "Example"
        encodedPassword shouldNotBe "test@example.com"
    }

    @Test
    fun `encryptSecretFields handles null values in secret fields`() {
        val raw = """{"s_secret":null,"s_token":"value"}"""
        val encryptionKey = ByteArray(32) { it.toByte() }

        val result = mapper.encryptSecretFields(raw, encryptionKey)

        val element = json.parseToJsonElement(result.rawJson).jsonObject
        element["s_secret"] shouldBe JsonNull

        val encodedToken = element["s_token"]!!.jsonPrimitive.content
        encodedToken.decodeBase64().isNotEmpty() shouldBe true
    }

    @Test
    fun `encryptSecretFields ignores non-string secret fields`() {
        val raw = """{"s_object":{"inner":"value"},"s_list":[1,2,3],"s_string":"text"}"""
        val encryptionKey = ByteArray(32) { it.toByte() }

        val result = mapper.encryptSecretFields(raw, encryptionKey)

        val element = json.parseToJsonElement(result.rawJson).jsonObject
        element["s_object"]!!.jsonObject["inner"]!!.jsonPrimitive.content shouldBe "value"
        element["s_list"]!!.jsonArray.size shouldBe 3

        val encodedString = element["s_string"]!!.jsonPrimitive.content
        encodedString.decodeBase64().isNotEmpty() shouldBe true
    }

    @Test
    fun `encryptSecretFields returns original json when invalid json provided`() {
        val raw = """not a valid json"""
        val encryptionKey = ByteArray(32) { it.toByte() }

        val result = mapper.encryptSecretFields(raw, encryptionKey)

        result.rawJson shouldBe raw
    }

    @Test
    fun `encryptSecretFields handles empty json object`() {
        val raw = """{}"""
        val encryptionKey = ByteArray(32) { it.toByte() }

        val result = mapper.encryptSecretFields(raw, encryptionKey)

        result.rawJson shouldBe raw
    }

    @Test
    fun `encryptSecretFields handles json with only non-secret fields`() {
        val raw = """{"name":"John","age":30,"email":"john@example.com"}"""
        val encryptionKey = ByteArray(32) { it.toByte() }

        val result = mapper.encryptSecretFields(raw, encryptionKey)

        val element = json.parseToJsonElement(result.rawJson).jsonObject
        element["name"]!!.jsonPrimitive.content shouldBe "John"
        element["age"]!!.jsonPrimitive.int shouldBe 30
        element["email"]!!.jsonPrimitive.content shouldBe "john@example.com"
    }

    @Test
    fun `encryptSecretFields handles json with only secret fields`() {
        val raw = """{"s_password":"pass123","s_token":"abc456"}"""
        val encryptionKey = ByteArray(32) { it.toByte() }

        val result = mapper.encryptSecretFields(raw, encryptionKey)

        val element = json.parseToJsonElement(result.rawJson).jsonObject
        val encodedPassword = element["s_password"]!!.jsonPrimitive.content
        val encodedToken = element["s_token"]!!.jsonPrimitive.content

        encodedPassword.decodeBase64().isNotEmpty() shouldBe true
        encodedToken.decodeBase64().isNotEmpty() shouldBe true
        encodedPassword shouldNotBe "pass123"
        encodedToken shouldNotBe "abc456"
    }

    @Test
    fun `encryptSecretFields handles empty string secret field`() {
        val raw = """{"s_empty":"","s_value":"test"}"""
        val encryptionKey = ByteArray(32) { it.toByte() }

        val result = mapper.encryptSecretFields(raw, encryptionKey)

        val element = json.parseToJsonElement(result.rawJson).jsonObject
        val encodedEmpty = element["s_empty"]!!.jsonPrimitive.content
        val encodedValue = element["s_value"]!!.jsonPrimitive.content

        encodedEmpty.decodeBase64().isNotEmpty() shouldBe true
        encodedValue.decodeBase64().isNotEmpty() shouldBe true
    }
}