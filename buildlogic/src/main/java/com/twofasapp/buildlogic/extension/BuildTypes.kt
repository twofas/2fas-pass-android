/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.buildlogic.extension

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Project

internal fun Project.applyBuildTypes(
    applicationExtension: ApplicationExtension,
) {
    applicationExtension.apply {
        buildTypes {
            getByName("debug") {
                isMinifyEnabled = false
                isDebuggable = true
                signingConfig = signingConfigs.getByName("debug")
                applicationIdSuffix = ".debug"
                manifestPlaceholders["fileProviderAuthority"] = "com.twofasapp.pass.debug.fileprovider"

            }
            create("internal") {
                isMinifyEnabled = true
                isDebuggable = false
                signingConfig = signingConfigs.getByName("internal")
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                matchingFallbacks += "release"
                applicationIdSuffix = ".internal"
                manifestPlaceholders["fileProviderAuthority"] = "com.twofasapp.pass.internal.fileprovider"
            }
            getByName("release") {
                isMinifyEnabled = true
                isDebuggable = false
                signingConfig = signingConfigs.getByName("release")
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                manifestPlaceholders["fileProviderAuthority"] = "com.twofasapp.pass.fileprovider"
            }
        }
    }
}