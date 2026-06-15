/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.buildlogic.extension

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.twofasapp.buildlogic.version.SdkConfig
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.applyKotlinAndroid(
    applicationExtension: ApplicationExtension,
) {
    applicationExtension.apply {
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
        }

        buildFeatures {
            buildConfig = true
        }

        packaging {
            resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
            resources.excludes.add("/META-INF/LICENSE.md")
            resources.excludes.add("/META-INF/LICENSE-notice.md")
            resources.excludes.add("META-INF/DEPENDENCIES")
            resources.excludes.add("META-INF/LICENSE")
            resources.excludes.add("META-INF/LICENSE.md")
            resources.excludes.add("META-INF/LICENSE-notice.md")
            resources.excludes.add("META-INF/LICENSE.txt")
            resources.excludes.add("META-INF/license.txt")
            resources.excludes.add("META-INF/NOTICE")
            resources.excludes.add("META-INF/NOTICE.txt")
            resources.excludes.add("META-INF/notice.txt")
            resources.excludes.add("META-INF/ASL2.0")
            resources.excludes.add("META-INF/INDEX.LIST")
        }

        testOptions {
            unitTests {
                isIncludeAndroidResources = true
            }
        }
    }

    configureKotlin()
    configureJavaToolchain()

    tasks.withType<Test>().configureEach {
        failOnNoDiscoveredTests = false
    }

    dependencies {
        add("coreLibraryDesugaring", libs.findLibrary("desugar").get())
        add("implementation", libs.findLibrary("timber").get())
    }
}

internal fun Project.applyKotlinAndroid(
    libraryExtension: LibraryExtension,
) {
    libraryExtension.apply {
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
        }

        buildFeatures {
            buildConfig = true
        }

        packaging {
            resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
            resources.excludes.add("/META-INF/LICENSE.md")
            resources.excludes.add("/META-INF/LICENSE-notice.md")
        }

        testOptions {
            unitTests {
                isIncludeAndroidResources = true
            }
        }
    }

    configureKotlin()
    configureJavaToolchain()

    tasks.withType<Test>().configureEach {
        failOnNoDiscoveredTests = false
    }

    dependencies {
        add("coreLibraryDesugaring", libs.findLibrary("desugar").get())
        add("implementation", libs.findLibrary("timber").get())
    }
}

private fun Project.configureJavaToolchain() {
    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
}

private fun Project.configureKotlin() {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21

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