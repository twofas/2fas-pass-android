/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.domain

sealed interface TagColor {
    val value: String

    data object Gray : TagColor {
        override val value = "gray"
    }

    data object Red : TagColor {
        override val value = "red"
    }

    data object Orange : TagColor {
        override val value = "orange"
    }

    data object Yellow : TagColor {
        override val value = "yellow"
    }

    data object Green : TagColor {
        override val value = "green"
    }

    data object Cyan : TagColor {
        override val value = "cyan"
    }

    data object Indigo : TagColor {
        override val value = "indigo"
    }

    data object Purple : TagColor {
        override val value = "purple"
    }

    data class Unknown(override val value: String) : TagColor

    companion object {
        fun values() = listOf(
            Gray,
            Red,
            Orange,
            Yellow,
            Green,
            Cyan,
            Indigo,
            Purple,
        )

        fun fromValue(value: String?): TagColor? {
            return values().firstOrNull { color -> color.value.equals(value, true) }
                ?: value?.let { Unknown(it) }
        }
    }
}