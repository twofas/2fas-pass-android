/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.buildlogic


import com.twofasapp.buildlogic.extension.getBuildExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class TwoFasComposePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            getBuildExtension()?.apply {
                buildFeatures {
                    compose = true
                }
            }
        }
    }
}
