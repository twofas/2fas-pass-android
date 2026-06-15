package com.twofasapp.buildlogic

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.VariantOutput
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.register
import java.io.File

internal class TwoFasArtifactsCopyPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val androidComponents = extensions.findByType(ApplicationAndroidComponentsExtension::class.java) ?: return

            androidComponents.onVariants { variant ->
                val variantName = variant.name

                // Only create tasks for release and internal variants
                if (variantName.contains("release", ignoreCase = true) ||
                    variantName.contains("internal", ignoreCase = true)
                ) {
                    val artifactsDir = File(rootProject.projectDir, "artifacts")

                    // APK copy task
                    val copyApkTaskName = "copy${variantName.capitalize()}Apk"
                    val assembleTaskName = "assemble${variantName.capitalize()}"

                    val copyApkTask = tasks.register<Copy>(copyApkTaskName) {
                        description = "Copies $variantName APK to artifacts folder"
                        group = "build"

                        from(layout.buildDirectory.dir("outputs/apk/$variantName"))
                        into(artifactsDir)
                        include("*.apk")

                        doFirst {
                            artifactsDir.mkdirs()
                        }

                        rename {
                            val newFileName = generateArtifactFileName(variant.outputs.first(), variantName, "apk")
                            logger.lifecycle("Copying APK to ${artifactsDir.absolutePath}/$newFileName")
                            newFileName
                        }
                    }

                    // AAB (bundle) copy task
                    val copyAabTaskName = "copy${variantName.capitalize()}Aab"
                    val bundleTaskName = "bundle${variantName.capitalize()}"

                    val copyAabTask = tasks.register<Copy>(copyAabTaskName) {
                        description = "Copies $variantName AAB to artifacts folder"
                        group = "build"

                        from(layout.buildDirectory.dir("outputs/bundle/$variantName"))
                        into(artifactsDir)
                        include("*.aab")

                        doFirst {
                            artifactsDir.mkdirs()
                        }

                        rename {
                            val newFileName = generateArtifactFileName(variant.outputs.first(), variantName, "aab")
                            logger.lifecycle("Copying AAB to ${artifactsDir.absolutePath}/$newFileName")
                            newFileName
                        }
                    }

                    // Hook tasks using afterEvaluate
                    afterEvaluate {
                        tasks.named(assembleTaskName) {
                            finalizedBy(copyApkTask)
                        }

                        tasks.named(bundleTaskName) {
                            finalizedBy(copyAabTask)
                        }
                    }
                }
            }
        }
    }
}

private fun generateArtifactFileName(
    variantOutput: VariantOutput,
    variantName: String,
    extension: String
): String {
    val versionName = variantOutput.versionName.orNull ?: "unknown"
    val versionCode = variantOutput.versionCode.orNull ?: 0

    return "TwoFas-Pass-$versionName-$versionCode-$variantName.$extension"
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { it.uppercase() }
}
