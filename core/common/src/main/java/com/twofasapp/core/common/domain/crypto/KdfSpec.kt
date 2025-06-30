/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.domain.crypto

import kotlinx.serialization.Serializable

sealed interface KdfSpec {
    data class Argon2id(
        val hashLength: Int = 32,
        val memoryMb: Int = 64,
        val iterations: Int = 3,
        val parallelism: Int = 4,
    ) : KdfSpec
}

@Serializable
class KdfSpecJson(
    val type: String,
    val hashLength: Int?,
    val memoryMb: Int?,
    val iterations: Int?,
    val parallelism: Int?,
)

fun KdfSpec.asEntity(): KdfSpecJson {
    return when (this) {
        is KdfSpec.Argon2id -> {
            KdfSpecJson(
                type = "argon2id",
                hashLength = hashLength,
                memoryMb = memoryMb,
                iterations = iterations,
                parallelism = parallelism,
            )
        }
    }
}

fun KdfSpecJson.asDomain(): KdfSpec {
    return when (type) {
        "argon2id" -> KdfSpec.Argon2id(
            hashLength = hashLength!!,
            memoryMb = memoryMb!!,
            iterations = iterations!!,
            parallelism = parallelism!!,
        )

        else -> KdfSpec.Argon2id()
    }
}