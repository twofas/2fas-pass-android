package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.data.main.FakeVaultCipher
import io.kotest.matchers.shouldBe
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
}