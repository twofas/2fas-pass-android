/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.buildlogic

import com.android.build.gradle.LibraryExtension
import com.twofasapp.buildlogic.extension.applyKotlinAndroid
import com.twofasapp.buildlogic.version.SdkConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class TwoFasAndroidLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<LibraryExtension> {
                applyKotlinAndroid(this)
                defaultConfig.targetSdk = SdkConfig.targetSdk
                defaultConfig.multiDexEnabled = true
            }
        }
    }
}
