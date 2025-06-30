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
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "com.twofasapp.data.main"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:android"))
    implementation(project(":core:di"))
    implementation(project(":core:network"))
    implementation(project(":core:crypto"))
    implementation(project(":data:cloud"))
    implementation(project(":data:security"))
    implementation(project(":data:settings"))
    implementation(project(":data:purchases"))

    implementation(libs.bundles.room)
    implementation(libs.kotlinCoroutines)
    implementation(libs.kotlinSerialization)
    implementation(libs.workManager)

    testImplementation(project(":testing:core"))
}