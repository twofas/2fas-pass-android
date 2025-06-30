/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.feature.password

object MasterPasswordValidator {
    fun minLength(password: String) = password.length >= 9
    fun passwordsMatch(password: String, passwordConfirm: String) =
        password.isNotBlank() &&
            passwordConfirm.isNotBlank() &&
            password == passwordConfirm

    fun valid(password: String, passwordConfirm: String): Boolean {
        return minLength(password) && passwordsMatch(password, passwordConfirm)
    }
}