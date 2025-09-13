/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

pluginManagement {
    includeBuild("buildlogic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "2fas-pass-android"
include(":app")
include(":testing:core")
include(":core")
include(":core:android")
include(":core:common")
include(":core:design")
include(":core:di")
include(":core:network")
include(":core:locale")
include(":core:crypto")

include(":data:settings")
include(":data:main")
include(":data:security")
include(":data:push")
include(":data:cloud")
include(":data:purchases")

include(":feature:startup")
include(":feature:main")
include(":feature:lock")
include(":feature:autofill")
include(":feature:decryptionkit")
include(":feature:home")
include(":feature:connect")
include(":feature:settings")
include(":feature:qrscan")
include(":feature:permissions")
include(":feature:externalimport")
include(":feature:cloudsync")
include(":feature:itemform")
include(":feature:importvault")
include(":feature:developer")
include(":feature:purchases")
include(":feature:quicksetup")
