/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.buildlogic.extension

import com.android.build.api.dsl.CommonExtension
import com.twofasapp.buildlogic.version.SdkConfig
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.applyKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        compileSdk = SdkConfig.compileSdk

        defaultConfig {
            minSdk = SdkConfig.minSdk

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            testInstrumentationRunnerArguments += mapOf(
                "clearPackageData" to "true",
                "disableAnalytics" to "true"
            )
        }

        compileOptions {
            isCoreLibraryDesugaringEnabled = true

            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        buildFeatures {
            buildConfig = true
        }

        packaging {
            resources {
                excludes += "META-INF/DEPENDENCIES"
                excludes += "META-INF/LICENSE"
                excludes += "META-INF/LICENSE.md"
                excludes += "META-INF/LICENSE-notice.md"
                excludes += "META-INF/LICENSE.txt"
                excludes += "META-INF/license.txt"
                excludes += "META-INF/NOTICE"
                excludes += "META-INF/NOTICE.txt"
                excludes += "META-INF/notice.txt"
                excludes += "META-INF/ASL2.0"
                excludes += "META-INF/INDEX.LIST"
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }

        testOptions {
            unitTests {
                isIncludeAndroidResources = true
            }
        }

        tasks.withType<Test>().configureEach {
            failOnNoDiscoveredTests = false
        }

        configureKotlin()
    }

    dependencies {
        add("coreLibraryDesugaring", libs.findLibrary("desugar").get())
        add("implementation", libs.findLibrary("timber").get())
    }
}


private fun Project.configureKotlin() {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17

            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
                "-opt-in=kotlin.Experimental",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                "-opt-in=androidx.lifecycle.compose.ExperimentalLifecycleComposeApi",
            )
        }
    }
}