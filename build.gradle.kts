/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

plugins {
    alias(libs.plugins.agpApplication) apply false
    alias(libs.plugins.agpLibrary) apply false

    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.kotlinParcelize) apply false
    alias(libs.plugins.gradleVersions)
    alias(libs.plugins.versionCatalogUpdate)
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.aboutLibraries) apply false
    id("com.google.gms.google-services") version "4.4.3" apply false
    id("com.google.firebase.crashlytics") version "3.0.5" apply false
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false

    alias(libs.plugins.kotlinComposeCompiler) apply false
}

tasks.register("prcheck", Exec::class) {
    commandLine = "./gradlew formatKotlin lintKotlin testDebugUnitTest".split(" ")
}

tasks.register("prformat", Exec::class) {
    commandLine = "./gradlew formatKotlin lintKotlin".split(" ")
}

tasks.register("libs", Exec::class) {
    commandLine = "./gradlew dependencyUpdates".split(" ")
}

tasks.register("libsFormat", Exec::class) {
    commandLine = "./gradlew versionCatalogFormat".split(" ")
}