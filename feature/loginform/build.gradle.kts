/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

plugins {
    alias(libs.plugins.twofasAndroidLibrary)
    alias(libs.plugins.twofasCompose)
    alias(libs.plugins.twofasLint)
    alias(libs.plugins.kotlinParcelize)
}

android {
    namespace = "com.twofasapp.feature.loginform"
}

dependencies {
    implementation(project(":core:android"))
    implementation(project(":core:common"))
    implementation(project(":core:design"))
    implementation(project(":core:di"))
    implementation(project(":core:locale"))

    implementation(project(":data:settings"))
    implementation(project(":data:main"))
    implementation(project(":feature:lock"))

    implementation(platform(libs.composeBom))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.viewModel)
    implementation(libs.biometric)
    implementation(libs.colorPicker)

    testImplementation(project(":testing:core"))
}