package com.twofasapp.data.security.crypto

import com.twofasapp.core.common.crypto.RandomGenerator
import com.twofasapp.core.common.ktx.decodeHex
import com.twofasapp.core.common.ktx.encodeHex
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEqualIgnoringCase
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test

@RunWith(Parameterized::class)
class SeedGeneratorTest(
    private val entropy: ByteArray,
    private val seedHex: String,
    private val saltHex: String,
    private val words: List<String>,
) {
    private val tested = SeedGenerator()

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(
                    "50e8bdc31c98db597a21ea72a9cefa60bd33668c".decodeHex(),
                    "764b9c40eab34b44e04fe7971d10eae9f525b5757b7be39fb39141faa89c01e3",
                    "26c96bc108546a840ff433f584731cb4",
                    listOf(
                        "extend",
                        "echo",
                        "ignore",
                        "decrease",
                        "misery",
                        "protect",
                        "trigger",
                        "diary",
                        "increase",
                        "example",
                        "salon",
                        "scrap",
                        "spring",
                        "rebuild",
                        "crack",
                    ),
                ),

                arrayOf(
                    "8444199d9fa4da4fcedd844c3f94f4e0359be2d0".decodeHex(),
                    "d3c224c31ba028b241fc2f8d7580e4691ab068d8ad8c09e5da6e52a6545dc4f4",
                    "01c4088d385a9e77f0131f272689d85c",
                    listOf(
                        "lounge",
                        "camp",
                        "guess",
                        "dismiss",
                        "ethics",
                        "child",
                        "derive",
                        "rack",
                        "equip",
                        "witness",
                        "diamond",
                        "scatter",
                        "fluid",
                        "vapor",
                        "drive",
                    ),
                ),

                arrayOf(
                    "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF".decodeHex(),
                    "9a8dcd3f9ff7aa3114e141f03c12989d363ea81fd74c02eea63c5f41489cb17a",
                    "e377ee6cca42d006b3e19ada4dd1eb85",
                    listOf("zoo", "zoo", "zoo", "zoo", "zoo", "zoo", "zoo", "zoo", "zoo", "zoo", "zoo", "zoo", "zoo", "zoo", "wrist"),
                ),

                arrayOf(
                    "0000000000000000000000000000000000000000".decodeHex(),
                    "de47c9b27eb8d300dbb5f2c353e632c393262cf06340c4fa7f1b40c4cbd36f90",
                    "376e94933e6ca72c8e5252fe4f4fbd45",
                    listOf(
                        "abandon",
                        "abandon",
                        "abandon",
                        "abandon",
                        "abandon",
                        "abandon",
                        "abandon",
                        "abandon",
                        "abandon",
                        "abandon",
                        "abandon",
                        "abandon",
                        "abandon",
                        "abandon",
                        "address",
                    ),
                ),
            )
        }
    }

    @Test
    fun `generate correct seed and words`() {
        val result = tested.generate(entropy)

        result.seedHex.shouldBeEqualIgnoringCase(seedHex)
        result.words.shouldBe(words)
        result.saltHex.shouldBe(saltHex)
    }

    @Test
    fun `restore seed from words`() {
        val result = tested.restore(words)

        result.entropyHex.shouldBeEqualIgnoringCase(entropy.encodeHex())
        result.seedHex.shouldBeEqualIgnoringCase(seedHex)
        result.saltHex.shouldBe(saltHex)
    }

    @Test
    fun `random pass`() {
        val seedGenerator = SeedGenerator()

        val generated = seedGenerator.generate(RandomGenerator.generate(20))
        val restored = seedGenerator.restore(generated.words)

        generated.entropyHex.shouldBeEqualIgnoringCase(restored.entropyHex)
        generated.seedHex.shouldBeEqualIgnoringCase(restored.seedHex)
        generated.saltHex.shouldBe(restored.saltHex)
    }
}