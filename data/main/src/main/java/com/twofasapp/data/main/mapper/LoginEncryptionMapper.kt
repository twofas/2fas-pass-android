/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.EncryptedLogin
import com.twofasapp.core.common.domain.EncryptedLoginUri
import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.common.domain.LoginSecurityType
import com.twofasapp.core.common.domain.LoginUri
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.data.main.VaultCipher
import com.twofasapp.data.main.domain.VaultKeysExpiredException

class LoginEncryptionMapper {

    fun decryptLogin(
        encryptedLogin: EncryptedLogin,
        vaultCipher: VaultCipher,
        decryptPassword: Boolean = false,
    ): Login? {
        return try {
            return with(encryptedLogin) {
                Login(
                    id = id,
                    vaultId = vaultId,
                    name = when (securityType) {
                        LoginSecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(name)
                        LoginSecurityType.Tier2 -> vaultCipher.decryptWithTrustedKey(name)
                        LoginSecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(name)
                    },
                    username = username?.let {
                        when (securityType) {
                            LoginSecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(it)
                            LoginSecurityType.Tier2 -> vaultCipher.decryptWithTrustedKey(it)
                            LoginSecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(it)
                        }
                    },
                    password = password?.let {
                        if (decryptPassword) {
                            SecretField.Visible(
                                when (securityType) {
                                    LoginSecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(it)
                                    LoginSecurityType.Tier2 -> vaultCipher.decryptWithSecretKey(it)
                                    LoginSecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(it)
                                },
                            )
                        } else {
                            SecretField.Hidden(it)
                        }
                    },
                    securityType = securityType,
                    uris = uris.map {
                        LoginUri(
                            matcher = it.matcher,
                            text = when (securityType) {
                                LoginSecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(it.text)
                                LoginSecurityType.Tier2 -> vaultCipher.decryptWithTrustedKey(it.text)
                                LoginSecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(it.text)
                            },
                        )
                    },
                    iconType = iconType,
                    iconUriIndex = iconUriIndex,
                    labelText = labelText?.let {
                        when (securityType) {
                            LoginSecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(it)
                            LoginSecurityType.Tier2 -> vaultCipher.decryptWithTrustedKey(it)
                            LoginSecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(it)
                        }
                    },
                    labelColor = labelColor,
                    customImageUrl = customImageUrl?.let {
                        when (securityType) {
                            LoginSecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(it)
                            LoginSecurityType.Tier2 -> vaultCipher.decryptWithTrustedKey(it)
                            LoginSecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(it)
                        }
                    },
                    notes = notes?.let {
                        when (securityType) {
                            LoginSecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(it)
                            LoginSecurityType.Tier2 -> vaultCipher.decryptWithTrustedKey(it)
                            LoginSecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(it)
                        }
                    },
                    tags = tags,
                    deleted = deleted,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                    deletedAt = deletedAt,
                )
            }
        } catch (e: VaultKeysExpiredException) {
            null
        }
    }

    fun encryptLogins(
        logins: List<Login>,
        vaultCipher: VaultCipher,
    ): List<EncryptedLogin> {
        return logins.map { encryptLogin(login = it, vaultCipher = vaultCipher) }
    }

    fun encryptLogin(
        login: Login,
        vaultCipher: VaultCipher,
    ): EncryptedLogin {
        return with(login) {
            EncryptedLogin(
                id = id,
                vaultId = vaultId,
                name = when (securityType) {
                    LoginSecurityType.Tier1 -> vaultCipher.encryptWithSecretKey(name)
                    LoginSecurityType.Tier2 -> vaultCipher.encryptWithTrustedKey(name)
                    LoginSecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey(name)
                },
                username = username?.let {
                    when (securityType) {
                        LoginSecurityType.Tier1 -> vaultCipher.encryptWithSecretKey(it)
                        LoginSecurityType.Tier2 -> vaultCipher.encryptWithTrustedKey(it)
                        LoginSecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey(it)
                    }
                },
                password = when (password) {
                    is SecretField.Hidden -> (password as SecretField.Hidden).value
                    is SecretField.Visible -> {
                        if ((password as SecretField.Visible).value.isBlank()) {
                            null
                        } else {
                            when (securityType) {
                                LoginSecurityType.Tier1 -> vaultCipher.encryptWithSecretKey((password as SecretField.Visible).value)
                                LoginSecurityType.Tier2 -> vaultCipher.encryptWithSecretKey((password as SecretField.Visible).value)
                                LoginSecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey((password as SecretField.Visible).value)
                            }
                        }
                    }

                    null -> null
                },
                securityType = securityType,
                uris = uris.map {
                    EncryptedLoginUri(
                        matcher = it.matcher,
                        text = when (securityType) {
                            LoginSecurityType.Tier1 -> vaultCipher.encryptWithSecretKey(it.text.trim())
                            LoginSecurityType.Tier2 -> vaultCipher.encryptWithTrustedKey(it.text.trim())
                            LoginSecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey(it.text.trim())
                        },
                    )
                },
                iconType = iconType,
                iconUriIndex = iconUriIndex,
                labelText = labelText?.let {
                    when (securityType) {
                        LoginSecurityType.Tier1 -> vaultCipher.encryptWithSecretKey(it)
                        LoginSecurityType.Tier2 -> vaultCipher.encryptWithTrustedKey(it)
                        LoginSecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey(it)
                    }
                },
                labelColor = labelColor,
                customImageUrl = customImageUrl?.let {
                    when (securityType) {
                        LoginSecurityType.Tier1 -> vaultCipher.encryptWithSecretKey(it)
                        LoginSecurityType.Tier2 -> vaultCipher.encryptWithTrustedKey(it)
                        LoginSecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey(it)
                    }
                },
                notes = notes?.let {
                    when (securityType) {
                        LoginSecurityType.Tier1 -> vaultCipher.encryptWithSecretKey(it)
                        LoginSecurityType.Tier2 -> vaultCipher.encryptWithTrustedKey(it)
                        LoginSecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey(it)
                    }
                },
                tags = tags,
                deleted = deleted,
                createdAt = createdAt,
                updatedAt = updatedAt,
                deletedAt = deletedAt,
            )
        }
    }

    fun decryptPassword(
        login: Login,
        vaultCipher: VaultCipher,
    ): String? {
        return try {
            val password = when (login.password) {
                is SecretField.Hidden -> (login.password as SecretField.Hidden).value
                is SecretField.Visible -> return (login.password as SecretField.Visible).value
                null -> null
            }

            password?.let {
                when (login.securityType) {
                    LoginSecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(it)
                    LoginSecurityType.Tier2 -> vaultCipher.decryptWithSecretKey(it)
                    LoginSecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(it)
                }
            }
        } catch (e: VaultKeysExpiredException) {
            null
        }
    }

    fun withVisiblePassword(
        login: Login?,
        vaultCipher: VaultCipher,
    ): Login? {
        return try {
            login?.copy(
                password = when (login.password) {
                    is SecretField.Hidden -> {
                        SecretField.Visible(
                            when (login.securityType) {
                                LoginSecurityType.Tier1 -> vaultCipher.decryptWithSecretKey((login.password as SecretField.Hidden).value)
                                LoginSecurityType.Tier2 -> vaultCipher.decryptWithSecretKey((login.password as SecretField.Hidden).value)
                                LoginSecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey((login.password as SecretField.Hidden).value)
                            },
                        )
                    }

                    is SecretField.Visible -> login.password
                    null -> null
                },
            )
        } catch (e: VaultKeysExpiredException) {
            null
        }
    }

    fun withHiddenPassword(
        login: Login?,
        vaultCipher: VaultCipher,
    ): Login? {
        return try {
            login?.copy(
                password = when (login.password) {
                    is SecretField.Hidden -> login.password
                    is SecretField.Visible -> {
                        SecretField.Hidden(
                            when (login.securityType) {
                                LoginSecurityType.Tier1 -> vaultCipher.encryptWithSecretKey((login.password as SecretField.Visible).value)
                                LoginSecurityType.Tier2 -> vaultCipher.encryptWithSecretKey((login.password as SecretField.Visible).value)
                                LoginSecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey((login.password as SecretField.Visible).value)
                            },
                        )
                    }

                    null -> null
                },
            )
        } catch (e: VaultKeysExpiredException) {
            null
        }
    }
}