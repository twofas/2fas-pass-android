/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.crypto

import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.ECFieldFp
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec
import java.security.spec.EllipticCurve

object EcKeyConverter {
    // Curve parameters for secp256r1 (NIST P-256)
    private val P = BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF", 16) // Prime modulo
    private val A = BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFC", 16) // Coefficient a
    private val B = BigInteger("5AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B", 16) // Coefficient b

    private val gx = BigInteger("6B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C296", 16)
    private val gy = BigInteger("4FE342E2FE1A7F9B8EE7EB4A7C0F9E162BCE33576B315ECECBB6406837BF51F5", 16)

    private val curve = EllipticCurve(
        /* field = */
        ECFieldFp(P),
        /* a = */
        A,
        /* b = */
        B,
    )
    private val secp256r1Spec = ECParameterSpec(
        /* curve = */
        curve,
        /* g = */
        ECPoint(gx, gy),
        /* n = */
        BigInteger("FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551", 16),
        /* h = */
        1,
    )

    /**
     * Converts uncompressed EC public key (65 bytes) to PublicKey for secp256r1
     */
    fun createPublicKey(ecKey: ByteArray): PublicKey {
        // Try to decompress
        val key = decompressKey(ecKey)

        // Extract X and Y coordinates (skip 0x04 prefix)
        val x = BigInteger(1, key.sliceArray(1..32)) // X from bytes 1-32
        val y = BigInteger(1, key.sliceArray(33..64)) // Y from bytes 33-64

        // Create EC point from X and Y
        val ecPoint = ECPoint(x, y)

        // Generate PublicKey using secp256r1 parameters
        val keyFactory = KeyFactory.getInstance("EC")
        return keyFactory.generatePublic(ECPublicKeySpec(ecPoint, secp256r1Spec))
    }

    /**
     *  Decompresses a compressed EC public key (33 bytes) to uncompressed format (65 bytes)
     */
    private fun decompressKey(key: ByteArray): ByteArray {
        // Validate key: must be 33 bytes and start with 0x02 (even Y) or 0x03 (odd Y)
        if ((key.size == 33 && key[0] in byteArrayOf(2, 3)).not()) {
            return key
        }

        // Extract X coordinate (skip prefix)
        val x = BigInteger(1, key.sliceArray(1..32))
        // Compute y^2 = x^3 + ax + b mod p (elliptic curve equation)
        val y = modularSquareRoot((x.pow(3) + A * x + B).mod(P), P)
        // Select correct y based on parity (0x02 = even, 0x03 = odd)
        val yFinal = if (y.mod(BigInteger.valueOf(2)) == (if (key[0] == 3.toByte()) BigInteger.ONE else BigInteger.ZERO)) y else P - y

        // Return uncompressed key: 0x04 prefix + X (32 bytes) + Y (32 bytes)
        return byteArrayOf(4) + x.toByteArray().padTo32() + yFinal.toByteArray().padTo32()
    }

    /**
     * Computes modular square root for y^2 mod p; uses (p+1)/4 exponent for p ≡ 3 mod 4
     */
    private fun modularSquareRoot(n: BigInteger, p: BigInteger): BigInteger {
        return if (n == BigInteger.ZERO) {
            BigInteger.ZERO
        } else {
            n.modPow((p + BigInteger.ONE) shr 2, p)
        }
    }

    /**
     * Pads or trims byte array to exactly 32 bytes
     */
    private fun ByteArray.padTo32() = when {
        size == 32 -> this // Already 32 bytes, return as-is
        size < 32 -> ByteArray(32 - size) + this // Pad with leading zeros
        else -> copyOfRange(size - 32, size) // Trim to last 32 bytes
    }
}