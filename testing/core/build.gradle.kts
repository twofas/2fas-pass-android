/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

plugins {
    alias(libs.plugins.twofasAndroidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

android {
    namespace = "com.twofasapp.testing"
}

dependencies {
    implementation(project(":core:android"))
    implementation(project(":core:common"))
    implementation(project(":core:design"))
    api(libs.bundles.ktor)
    api(libs.bundles.elmyr)
    api(libs.junit)
    api(libs.mockk)
    api(libs.kotest)
    api(libs.kotlinTestJunit)
    api(libs.kotlinCoroutinesTest)
    api(libs.turbine)
    api(libs.koinTest)
    api(libs.koinTestJunit)
    api(libs.robolectric)
    api(libs.testCore)
    api(libs.espressoCore)
    api(libs.uiTestJunit)
    api(libs.slf4jNoOp)

    debugImplementation(libs.uiTestManifest)
}