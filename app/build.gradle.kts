/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

plugins {
    alias(libs.plugins.twofasAndroidApplication)
    alias(libs.plugins.twofasCompose)
    alias(libs.plugins.twofasLint)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kotlinKsp)
    alias(libs.plugins.aboutLibraries)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.twofasapp.pass"

    defaultConfig {
        applicationId = "com.twofasapp.pass"
        versionName = "1.5.0"
        versionCode = 33
    }

    applicationVariants.all {
        outputs.all {
            val output = this as? com.android.build.gradle.internal.api.BaseVariantOutputImpl
            output?.outputFileName = "TwoFas-Pass-$versionName-${versionCode}.apk"
        }
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

dependencies {
    implementation(project(":core:android"))
    implementation(project(":core:common"))
    implementation(project(":core:design"))
    implementation(project(":core:di"))
    implementation(project(":core:crypto"))
    implementation(project(":core:network"))
    implementation(project(":core:locale"))

    implementation(project(":data:settings"))
    implementation(project(":data:main"))
    implementation(project(":data:security"))
    implementation(project(":data:push"))
    implementation(project(":data:cloud"))
    implementation(project(":data:purchases"))

    implementation(project(":feature:startup"))
    implementation(project(":feature:main"))
    implementation(project(":feature:lock"))
    implementation(project(":feature:decryptionkit"))
    implementation(project(":feature:autofill"))
    implementation(project(":feature:home"))
    implementation(project(":feature:connect"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:qrscan"))
    implementation(project(":feature:externalimport"))
    implementation(project(":feature:cloudsync"))
    implementation(project(":feature:itemform"))
    implementation(project(":feature:developer"))
    implementation(project(":feature:purchases"))
    implementation(project(":feature:quicksetup"))

    implementation(platform(libs.composeBom))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.viewModel)
    implementation(libs.bundles.room)
    implementation(libs.bundles.coil)
    implementation(libs.bundles.aboutLibraries)
    implementation(libs.revenuecat)
    implementation(libs.revenuecatUi)
    ksp(libs.roomCompiler)
    implementation(libs.appcompat)
    implementation(libs.coreSplash)
    implementation(libs.dataStore)
    implementation(libs.biometric)
    implementation(libs.autofill)
    implementation(libs.kotlinSerialization)
    implementation(libs.truetime)
    implementation(platform(libs.firebaseBom))
    implementation(libs.firebaseMessaging)
    implementation(libs.firebaseCrashlytics)
    implementation(libs.playServicesCorutines)
    implementation(libs.workManager)

    debugImplementation(libs.pluto)
    debugImplementation(libs.plutoRoom)
    debugImplementation(libs.plutoDataStore)
    debugImplementation(libs.plutoLogger)

    internalImplementation(libs.plutoNoOp)
    internalImplementation(libs.plutoRoomNoOp)
    internalImplementation(libs.plutoDataStoreNoOp)
    internalImplementation(libs.plutoLoggerNoOp)

    releaseImplementation(libs.plutoNoOp)
    releaseImplementation(libs.plutoRoomNoOp)
    releaseImplementation(libs.plutoDataStoreNoOp)
    releaseImplementation(libs.plutoLoggerNoOp)

    testImplementation(project(":testing:core"))
}
