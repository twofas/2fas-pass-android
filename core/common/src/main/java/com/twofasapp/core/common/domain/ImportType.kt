/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.domain

enum class ImportType(val displayName: String) {
    Bitwarden("Bitwarden"),
    OnePassword("1Password"),
    ProtonPass("Proton Pass"),
    Chrome("Chrome"),
    MicrosoftEdge("Microsoft Edge"),
    Enpass("Enpass"),
    LastPass("LastPass"),
    DashlaneDesktop("Dashlane (Desktop)"),
    DashlaneMobile("Dashlane (Mobile)"),
    AppleDesktop("Apple Passwords (Desktop)"),
    AppleMobile("Apple Passwords (Mobile)"),
    Firefox("Firefox"),
    KeePass("KeePass"),
    KeePassXC("KeePassXC"),
    Keeper("Keeper"),
}