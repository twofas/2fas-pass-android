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
    namespace = "com.twofasapp.data.cloud"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:android"))
    implementation(project(":core:di"))

    implementation(libs.kotlinCoroutines)
    implementation(libs.dataStore)
    implementation(libs.kotlinSerialization)
    implementation(platform(libs.composeBom))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.ktor)
    implementation(libs.workManager)

    implementation(libs.credentials)
    implementation(libs.credentialsPlayServices)

    implementation(libs.googleId)
    implementation(libs.googleAuth){
        exclude("org.apache.httpcomponents", "guava-jdk5")
        exclude("com.google.http-client", "google-http-client")
    }
    implementation(libs.googleDrive){
        exclude("org.apache.httpcomponents", "guava-jdk5")
        exclude("com.google.http-client", "google-http-client")
    }
    implementation(libs.googleApiClientAndroid){
        exclude("org.apache.httpcomponents", "guava-jdk5")
        exclude("com.google.http-client", "google-http-client")
    }
    implementation(libs.googleApiHttpClientFork)
}