/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.build

import android.os.Build
import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.core.common.build.BuildVariant
import com.twofasapp.pass.BuildConfig

class AppBuildImpl : AppBuild {

    override val packageName: String = BuildConfig.APPLICATION_ID

    override val debuggable: Boolean = BuildConfig.DEBUG

    override val os: String = "android"

    override val versionName: String = BuildConfig.VERSION_NAME

    override val versionCode: Int = BuildConfig.VERSION_CODE

    override val buildVariant: BuildVariant = when (BuildConfig.BUILD_TYPE.lowercase()) {
        "release" -> BuildVariant.Release
        "internal" -> BuildVariant.Internal
        "debug" -> BuildVariant.Debug
        else -> error("Unknown build variant!")
    }

    override val systemSdkVersion: Int = Build.VERSION.SDK_INT
}