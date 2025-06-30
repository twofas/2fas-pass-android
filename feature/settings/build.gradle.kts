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
    namespace = "com.twofasapp.feature.settings"
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
    implementation(project(":data:purchases"))

    implementation(project(":feature:lock"))
    implementation(project(":feature:decryptionkit"))
    implementation(project(":feature:cloudsync"))
    implementation(project(":feature:permissions"))
    implementation(project(":feature:qrscan"))
    implementation(project(":feature:importvault"))
    implementation(project(":feature:purchases"))

    implementation(platform(libs.composeBom))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.viewModel)
    implementation(libs.bundles.coil)
    implementation(libs.bundles.aboutLibraries)
}