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
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "com.twofasapp.feature.cloudsync"
}

dependencies {
    implementation(project(":core:android"))
    implementation(project(":core:common"))
    implementation(project(":core:design"))
    implementation(project(":core:di"))
    implementation(project(":core:locale"))

    implementation(project(":data:settings"))
    implementation(project(":data:main"))
    implementation(project(":data:cloud"))
    implementation(project(":data:security"))

    implementation(project(":feature:purchases"))

    implementation(platform(libs.composeBom))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.viewModel)
    implementation(libs.bundles.ktor)
    implementation(libs.kotlinSerialization)
}