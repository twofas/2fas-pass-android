/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly("com.android.tools.build:gradle:${libs.versions.agp.get()}")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    compileOnly("org.jmailen.gradle:kotlinter-gradle:${libs.versions.ktlint.get()}")
}

gradlePlugin {
    @Suppress("DSL_SCOPE_VIOLATION")
    plugins {
        register("TwoFasComposePlugin") {
            id = "twofas.compose"
            implementationClass = "com.twofasapp.buildlogic.TwoFasComposePlugin"
        }

        register("TwoFasAndroidLibraryPlugin") {
            id = "twofas.androidLibrary"
            implementationClass = "com.twofasapp.buildlogic.TwoFasAndroidLibraryPlugin"
        }

        register("TwoFasAndroidApplicationPlugin") {
            id = "twofas.androidApplication"
            implementationClass = "com.twofasapp.buildlogic.TwoFasAndroidApplicationPlugin"
        }

        register("TwoFasLintPlugin") {
            id = "twofas.lint"
            implementationClass = "com.twofasapp.buildlogic.TwoFasLintPlugin"
        }
    }
}