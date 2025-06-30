/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

plugins {
    alias(libs.plugins.twofasAndroidLibrary)
    alias(libs.plugins.twofasLint)
    alias(libs.plugins.twofasCompose)
}

android {
    namespace = "com.twofasapp.core.locale"
}

dependencies {
    implementation(platform(libs.composeBom))
    implementation(libs.bundles.compose)
}