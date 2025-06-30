/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.build

import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.core.common.build.BuildVariant
import com.twofasapp.core.common.build.LocalConfig
import com.twofasapp.pass.BuildConfig

class LocalConfigImpl(
    appBuild: AppBuild,
) : LocalConfig {
    override val googleAuthClientId: String = when (appBuild.buildVariant) {
        BuildVariant.Release -> BuildConfig.GoogleAuthClientIdRelease
        BuildVariant.Internal -> BuildConfig.GoogleAuthClientIdInternal
        BuildVariant.Debug -> BuildConfig.GoogleAuthClientIdDebug
    }
    override val revenueCatPublicKey: String = when (appBuild.buildVariant) {
        BuildVariant.Release -> BuildConfig.RevenueCatRelease
        BuildVariant.Internal -> BuildConfig.RevenueCatInternal
        BuildVariant.Debug -> BuildConfig.RevenueCatDebug
    }
}