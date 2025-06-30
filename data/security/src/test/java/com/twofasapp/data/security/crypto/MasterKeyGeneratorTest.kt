package com.twofasapp.data.security.crypto

import com.twofasapp.core.common.domain.crypto.KdfSpec
import com.twofasapp.core.common.ktx.decodeHex
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test

class MasterKeyGeneratorTest {
    private val password: String = "MySecret123!"
    private val seedHex: String = "764b9c40eab34b44e04fe7971d10eae9f525b5757b7be39fb39141faa89c01e3"
    private val saltHex: String = "26c96bc108546a84"
    private val kdfSpec: KdfSpec = KdfSpec.Argon2id()

    private val kdfCalculator: KdfCalculator = mockk()
    private val tested = MasterKeyGenerator(kdfCalculator)

    @Test
    fun `verify correct kdf input`() {
        every { kdfCalculator.kdf(any(), any(), any()) } returns byteArrayOf()

        tested.generate(
            password = password,
            seedHex = seedHex,
            saltHex = saltHex,
            kdfSpec = kdfSpec,
        )

        verify {
            kdfCalculator.kdf(
                input = "764b9c40eab34b44e04fe7971d10eae9f525b5757b7be39fb39141faa89c01e34d7953656372657431323321".decodeHex(),
                salt = saltHex.decodeHex(),
                kdfSpec = kdfSpec,
            )
        }
    }
}