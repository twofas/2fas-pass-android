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
}

android {
    namespace = "com.twofasapp.feature.home"
}

dependencies {
    implementation(project(":core:android"))
    implementation(project(":core:common"))
    implementation(project(":core:design"))
    implementation(project(":core:di"))
    implementation(project(":core:locale"))

    implementation(project(":data:main"))
    implementation(project(":data:settings"))
    implementation(project(":data:cloud"))
    implementation(project(":data:purchases"))
    implementation(project(":feature:itemform"))

    implementation(project(":feature:developer"))
    implementation(project(":feature:purchases"))

    implementation(platform(libs.composeBom))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.viewModel)
    implementation(libs.webkit)
}