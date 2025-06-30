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
import java.io.File
import java.io.FileInputStream
import java.util.Properties

internal fun Project.applySigningConfigs(
    applicationExtension: ApplicationExtension,
) {
    applicationExtension.apply {

        val localConfig = Properties().apply {
            load(FileInputStream(File(rootProject.rootDir, "config/config.properties")))
        }

        signingConfigs {
            getByName("debug") {
                storeFile = file("../config/debug_signing.jks")
                storePassword = localConfig.getProperty("debug.storePassword")
                keyAlias = localConfig.getProperty("debug.keyAlias")
                keyPassword = localConfig.getProperty("debug.keyPassword")
            }
            create("internal") {
                storeFile = file("../config/internal_signing.jks")
                storePassword = localConfig.getProperty("internal.storePassword")
                keyAlias = localConfig.getProperty("internal.keyAlias")
                keyPassword = localConfig.getProperty("internal.keyPassword")
            }
            create("release") {
                storeFile = file("../config/release_upload.jks")
                storePassword = localConfig.getProperty("releaseUpload.storePassword")
                keyAlias = localConfig.getProperty("releaseUpload.keyAlias")
                keyPassword = localConfig.getProperty("releaseUpload.keyPassword")
            }
        }
    }
}